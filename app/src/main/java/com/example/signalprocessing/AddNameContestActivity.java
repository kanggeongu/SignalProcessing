package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class AddNameContestActivity extends AppCompatActivity {

    // 허가
    private static final int GALLERY_CODE=10;
    private static final int CAMERA_CODE=20;
    private Uri cameraUri;
    private String imageFilePath;

    // 파이어베이스 스토리지
    private FirebaseStorage storage = FirebaseStorage.getInstance("gs://practice1-243d9.appspot.com");
    private StorageReference storageReference = storage.getReference();
    private StorageReference imgReference;

    // 파이어베이스 데이터베이스
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    // 업로드에 필요한 전역 변수
    private Uri file,downloadUri;
    private UploadTask uploadTask;
    private String picture="";

    // 진행 상황
    private ProgressDialog pdialog=null;

    private String selected="알 수 없음";
    private List<String> genders=new ArrayList<>();

    private User user;
    private String gender;

    // xml 위젯
    private LinearLayout linearImage, linearEditText;
    private EditText editTextNameContestLocation, editTextNameContestCharacteristic, editTextSimpleSentence;
    private ImageView imageViewAddNameContest;
    private Spinner spinnerNameContestGender;

    private int[] endDays = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_name_contest);

        user = (User)getIntent().getSerializableExtra("userInformation");
        initPallete();

        pdialog=new ProgressDialog(AddNameContestActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }

    private void initPallete() {
        linearImage = (LinearLayout)findViewById(R.id.linearImage);
        linearEditText = (LinearLayout)findViewById(R.id.linearEditText);

        editTextNameContestLocation = (EditText) findViewById(R.id.editTextNameContestLocation);
        editTextNameContestCharacteristic = (EditText)findViewById(R.id.editTextNameContestCharacteristic);
        editTextSimpleSentence = (EditText)findViewById(R.id.editTextSimpleSentence);

        spinnerNameContestGender = (Spinner)findViewById(R.id.spinnerNameContestGender);
        setSpinner(spinnerNameContestGender);

        imageViewAddNameContest = (ImageView)findViewById(R.id.imageViewAddNameContest);
    }

    private void setSpinner(Spinner spinner) {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void ToastText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void addName() {
        if (file == null) {
            ToastText("사진을 추가해주세요.");
            return;
        }

        String characteristic = editTextNameContestCharacteristic.getText().toString();
        if(characteristic.equals("")){
            ToastText("가벼운 특징이라도 하나 적어주세요!");
            return;
        }

        String simpleSentence = editTextSimpleSentence.getText().toString();
        if(simpleSentence.equals("")){
            ToastText("해당 공모에 대한 한마디 부탁합니다");
            return;
        }

        String location = editTextNameContestLocation.getText().toString();
        if(location.equals("")){
            ToastText("어디에서 봤나요?");
            return;
        }

        new AlertDialog.Builder(AddNameContestActivity.this)
                .setTitle("이름 공모전 추가")
                .setMessage("중복된 공모가 없는지 확인해주시고 올려주세요")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pdialog.show();
                        uploadStorage();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastText("취소하였습니다");
                    }
                })
                .show();
    }

    private int leap_year(int year, int month) {
        if(month != 2) return 0;
        if(year % 400 == 0) return 1;
        if(year % 100 ==0) return 0;
        if(year % 4 == 0)return 1;
        return 0;
    }

    private int cal_time(int time) {
        for(int i=0;i<7;i++) {
            time += 1;
            int year = time/10000;
            time %= 10000;
            int month = time / 100;
            int day = time % 100;

            if(day > endDays[month] + leap_year(year, month)) {
                day = 1;
                if(++month == 13) {
                    month = 1;
                    year++;
                }
            }

            time = year * 10000 + month * 100 + day;
        }

        return time;
    }

    private void uploadStorage() {
        final Long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String startTime = simpleDateFormat.format(mDate);
        int time = Integer.parseInt(startTime.substring(0,8));
        /*time -= 8;
        startTime = time + startTime.substring(8, 14);*/

        time = cal_time(time);
        String endTime = time + startTime.substring(8, 14);

        String path = "NameContest/" + Long.toString(now) + ".jpg";
        imgReference = storageReference.child(path);
        if(file == null) {
            Toast.makeText(this, "이미지 파일이 존재하지 않습니다!", Toast.LENGTH_SHORT).show();
            return;
        }

        String location = editTextNameContestLocation.getText().toString();
        String characteristic = editTextNameContestCharacteristic.getText().toString();
        String oneSentence = editTextSimpleSentence.getText().toString();

        final NameContestData nameContestData = new NameContestData(Long.toString(now), path,
                location, gender, characteristic, startTime, endTime, oneSentence, user.getUserEmail(), "심사중");

        uploadTask = imgReference.putFile(file);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.v("ADDNAMECONTEST", "addUri()");
                addUri(now, nameContestData);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void addUri(final Long now, final NameContestData nameContestData) {

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
                    nameContestData.setImage(downloadUri.toString());
                    Log.v("ADDNAMECONTEST", "addFinalData()");
                    addFinalData(now, nameContestData);
                }
            }
        });
    }

    private void addFinalData(final Long now, final NameContestData nameContestData) {
        databaseReference.child("NameContests").child(user.getUserUniv()).child(Long.toString(now)).setValue(nameContestData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    pdialog.dismiss();
                    Log.v("ADDNAMECONTEST", "Finish");
                    ToastText("업로드가 완료되었습니다");
                }
                else {
                    ToastText("업로드에 실패하였습니다");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && data != null) {

            Uri uri = data.getData();
            Log.v("12345", uri.toString());

            if(uri != null) {
                file = Uri.fromFile(new File(getPath(uri)));
                try{
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageViewAddNameContest.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        else if (requestCode == CAMERA_CODE && resultCode==RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;
            try{
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
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
            imageViewAddNameContest.setImageBitmap(bitmap);
            String imageSaveUri=MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"사진 저장","저장되었다");
            Uri uri = Uri.parse(imageSaveUri);
            file=uri;
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
        }
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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

    private void goToAlbum() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,GALLERY_CODE);
    }

    private void takePhoto() {
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

    private void imageUpload() {

        new AlertDialog.Builder(AddNameContestActivity.this)
                .setTitle("사진 업로드")
                .setNegativeButton("사진촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("AddName", "takePhoto()");
                        takePhoto();
                    }
                })
                .setPositiveButton("앨범선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("AddName", "goToAlbum()");
                        goToAlbum();
                    }
                })
                .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastText("취소하였습니다");
                    }
                })
                .show();
    }

    public void onClickAddNameContest(View v) {
        switch (v.getId()) {
            case R.id.buttonAddNameContest:
                addName();
                break;
            case R.id.imageViewAddNameContest:
                imageUpload();
                break;
        }
    }
}
