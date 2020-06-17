package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class writeArticleActivity extends AppCompatActivity {

    private EditText contents;
    private Button btn_save;
    private Button camera;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private ImageView selectedImage;
    private UploadTask uploadTask;
    private Uri file;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private StorageReference articleRef;
    private Uri downloadUri;
    private String imageFilePath;
    private Uri cameraUri;
    private Calendar c = Calendar.getInstance();
    private User user;
    private Article article;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private Long now = System.currentTimeMillis();
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private ProgressDialog pdialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_article);

        contents = findViewById(R.id.contents);
        btn_save = findViewById(R.id.btn_save);
        camera = this.findViewById(R.id.camera);
        selectedImage = findViewById(R.id.selectedImage);

        user = (User)getIntent().getSerializableExtra("userInformation");

        tedPermission();


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDialog();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                article = new Article(Long.toString(now), user.getUserUniv(), user.getUserName(), contents.getText().toString(), "", dateFormat.format(c.getTime()));
                if(file!=null){
                    upload(file);
                }
                databaseReference.child("Articles").child(user.getUserUniv()).child(Long.toString(now)).setValue(article).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("123213", "finish");
                        pdialog.dismiss();
                        finish();
                    }
                });
            }
        });
    }

    public void upload(Uri file){
        Log.v("123123", "upload()");
        pdialog=new ProgressDialog(writeArticleActivity.this);
        pdialog.setTitle("업로드 중입니다");
        pdialog.show();
        uploadTask = articleRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(writeArticleActivity.this, "실패", Toast.LENGTH_SHORT).show();
                Log.v("123123", "fail");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.v("132123", "???");
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        Log.v("12312", "!!!");
                        return articleRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Log.v("123123", "success");
                            downloadUri = task.getResult();
                            c.setTimeInMillis(now);
                            c.add(Calendar.DATE, 7);
                            Log.v("123123", "before null");
                            assert downloadUri != null;
                            Log.v("123123", "after null");
                            article.setImage(downloadUri.toString());
                            databaseReference.child("Articles").child(user.getUserUniv()).child(Long.toString(now)).setValue(article).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.v("123213", "finish");
                                    pdialog.dismiss();
                                    finish();
                                }
                            });

                        } else {
                            // Handle failures
                            // ...
                            Log.v("123123", "why not?");
                        }
                    }
                });
            }
        });
    }

    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    private void makeDialog(){

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(writeArticleActivity.this);
        alt_bld.setTitle("사진 업로드").setCancelable(false).setPositiveButton("사진촬영", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.v("알림", "다이얼로그 > 사진촬영 선택");
                // 사진 촬영 클릭
                takePhoto();

            }

        }).setNeutralButton("앨범선택", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                Log.v("알림", "다이얼로그 > 앨범선택 선택");
                //앨범에서 선택
                goToAlbum();
            }
        }).setNegativeButton("취소   ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.v("알림", "다이얼로그 > 취소 선택");
                // 취소 클릭. dialog 닫기.
                dialog.cancel();
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
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
                startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        }
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            return;
        }
        switch (requestCode){
            case PICK_FROM_ALBUM : {
                //앨범에서 가져오기
                Uri uri=data.getData();
                if(uri!=null){
                    file=Uri.fromFile(new File(getPath(uri)));
                    try{
                        Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        selectedImage.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    articleRef=storageRef.child("articles/"+file.getLastPathSegment());
                    Log.e("Error",articleRef.toString());
                }
                break;
            }
            case PICK_FROM_CAMERA : {
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
                selectedImage.setImageBitmap(bitmap);
                String imageSaveUri=MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"사진 저장","저장되었다");
                Uri uri = Uri.parse(imageSaveUri);
                file = uri;
                articleRef=storageRef.child("articles/"+file.getLastPathSegment());
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
                break;
            }
        }
    }

    public String getPath(Uri uri){
        String [] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        assert cursor != null;
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
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
    private Bitmap rotate(Bitmap bitmap, float degree){
        Matrix matrix=new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    private int exifOrientationToDegree(int exifOrientation){
        if(exifOrientation== ExifInterface.ORIENTATION_ROTATE_90){
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
}