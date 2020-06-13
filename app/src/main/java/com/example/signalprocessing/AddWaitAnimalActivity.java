package com.example.signalprocessing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.service.autofill.Validator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddWaitAnimalActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    // xml 위젯
    private Button btnAdd,btnGallery,btnCamera;
    private TextView textName,textMean,textLocation,textFeature;
    private ImageView imgPhoto;
    private Spinner spinner;

    //private String spinnerValue="선택 안함";
    //private String writer;
    // 허가
    private static final int GALLERY_CODE=10;
    private static final int CAMERA_CODE=20;
    private Uri cameraUri;
    private String imageFilePath;

    // 파이어베이스 스토리지
    private FirebaseStorage storage=FirebaseStorage.getInstance("gs://signalprocessing-867b3.appspot.com");
    private StorageReference sRef=storage.getReference();
    private StorageReference imgReference;

    // 유저
    //private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private User user=null;

    // 파이어베이스 데이터베이스
    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();

    // 업로드에 필요한 전역 변수
    private Uri file,downloadUri;
    private UploadTask uploadTask;
    private String picture="";

    // 진행 상황
    private ProgressDialog pdialog=null;

    private String selected="알 수 없음";
    private List<String> genders=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wait_animal);

        user=(User)getIntent().getSerializableExtra("userInfo");

        btnCamera=findViewById(R.id.addwait_btn_Camera);
        btnAdd=findViewById(R.id.addwait_btn_upload);
        btnGallery=findViewById(R.id.addwait_btn_addPhoto);
        textName=findViewById(R.id.addwait_edit_name);
        textMean=findViewById(R.id.addwait_edit_meaning);
        textLocation=findViewById(R.id.addwait_edit_location);
        textFeature=findViewById(R.id.addwait_edit_feature);
        imgPhoto=findViewById(R.id.addwait_img_img);
        spinner=findViewById(R.id.addwait_spinner_gender);

        loadGender();
        spinner.setSelection(0,false);
        spinner.setOnItemSelectedListener(this);

        pdialog=new ProgressDialog(AddWaitAnimalActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=textName.getText().toString().trim();
                String mean=textMean.getText().toString().trim();
                String gender=selected;
                String location=textLocation.getText().toString().trim();
                String feature=textFeature.getText().toString().trim();
                if(name.equals("")){
                    showToast("이름은 필수항목입니다");
                }
                else if(mean.equals("")){
                    showToast("이름 뜻은 필수항목입니다");
                }
                else if(location.equals("")){
                    showToast("발견 위치는 필수항목입니다");
                }
                else if(feature.equals("")){
                    showToast("특징은 필수항목입니다. 반려될 수 있습니다");
                }
                else if(file==null){
                    showToast("사진은 필수항목입니다");
                }
                else{
                    WaitItem item=new WaitItem(user.getUserName(),name,mean,location,feature,gender,picture,"심사중");
                    pdialog.setTitle("업로드 중입니다");
                    pdialog.show();
                    addItem(item);
                }
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,GALLERY_CODE);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    public void takePhoto(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            File photoFile=null;
            try{
                photoFile=createImageFile();
            }catch(IOException e){
                e.printStackTrace();
            }
            if (photoFile != null) {
                cameraUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                startActivityForResult(intent, CAMERA_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public void loadGender(){
        genders.clear();
        genders.add("알 수 없음");
        genders.add("수컷");
        genders.add("암컷");
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,genders);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE&&data!=null){
            Uri uri=data.getData();
            if(uri!=null){
                file=Uri.fromFile(new File(getPath(uri)));
                try{
                    Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    imgPhoto.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imgReference=sRef.child("waits/"+file.getLastPathSegment());
                Log.e("Error",imgReference.toString());
            }
        }
        else if(requestCode==CAMERA_CODE&&resultCode==RESULT_OK){
            Bitmap bitmap= BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif=null;
            try{
                exif=new ExifInterface(imageFilePath);
            }catch(IOException e){
                e.printStackTrace();
            }
            int exifOrientation;
            int exifDegree;
            if(exif!=null){
                exifOrientation=exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
                exifDegree=exifOrientationToDegree(exifOrientation);
            }
            else{
                exifDegree=0;
            }
            bitmap=rotate(bitmap,exifDegree);
            imgPhoto.setImageBitmap(bitmap);
            String imageSaveUri=MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"사진 저장","저장되었다");
            Uri uri = Uri.parse(imageSaveUri);
            file=uri;
            imgReference=sRef.child("waits/"+file.getLastPathSegment());
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
        }
    }

    private Bitmap rotate(Bitmap bitmap,float degree){
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    private int exifOrientationToDegree(int exifOrientation){
        if(exifOrientation==ExifInterface.ORIENTATION_ROTATE_90){
            return 90;
        }
        else if(exifOrientation==ExifInterface.ORIENTATION_ROTATE_180){
            return 180;
        }
        else if(exifOrientation==ExifInterface.ORIENTATION_ROTATE_270){
            return 270;
        }
        return 0;
    }

    public String getPath(Uri uri){
        String [] proj={MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this,uri,proj,null,null,null);

        Cursor cursor= cursorLoader.loadInBackground();
        int index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();
        return cursor.getString(index);
    }

    public void addItem(final WaitItem item){
        if(file!=null){
            uploadTask=imgReference.putFile(file);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    addUri(item);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
        else{
            addFinalData(item);
        }
    }

    private void addFinalData(final WaitItem data){
        mRef.child("Waits").child("경북대학교").push().setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("업로드 완료");
                    pdialog.dismiss();
                    updateUI();
                }
                else{
                    showToast("업로드 실패");
                }
            }
        });
    }

    private void addUri(final WaitItem data){
        Task<Uri> urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imgReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    downloadUri=task.getResult();
                    assert downloadUri != null;
                    data.setPicture(downloadUri.toString());
                    addFinalData(data);
                }
            }
        });
    }

    public void updateUI(){
        Intent intent=new Intent(AddWaitAnimalActivity.this,WaitAnimalActivity.class);
        intent.putExtra("userInfo",user);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUI();
    }

    public void showToast(String toastMessage){
        Toast.makeText(AddWaitAnimalActivity.this,toastMessage,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
        selected= parent.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
