package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private Button esignup,check;
    private String confirmed="";
    private boolean isValid=true;

    private TextInputLayout textInputPassword;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        esignup=(Button)findViewById(R.id.signup_btn_signup);
        check=(Button)findViewById(R.id.signup_btn_check);

        textInputPassword=findViewById(R.id.signup_input_password);
        textInputEmail=findViewById(R.id.signup_input_email);
        textInputName=findViewById(R.id.signup_input_nickname);

        esignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=textInputEmail.getEditText().getText().toString().trim();
                String pw=textInputPassword.getEditText().getText().toString().trim();
                String nickname=textInputName.getEditText().getText().toString().trim();
                validatePassword(pw);
                validateEmail(email);
                validateName(nickname);
                if(isValid){
                    isRestricted(email,pw,nickname);
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname=textInputName.getEditText().getText().toString().trim();
                if(nickname.equals("")){
                    showToast("닉네임을 입력하세요");
                }
                else {
                    chkUser(nickname);
                }
            }
        });
    }

    private void validatePassword(String password){
        if(password.length()<8){
            textInputPassword.setError("비밀번호는 8자리 이상이어야 합니다");
            isValid=false;
        }
        else{
            textInputPassword.setError(null);
        }
    }

    private void validateEmail(String email){
        if(email.length()==0){
            textInputEmail.setError("이메일을 입력하세요");
            isValid=false;
        }
        else{
            textInputEmail.setError(null);
        }
    }

    private void validateName(String name){
        if(name.equals("")){
            textInputName.setError("닉네임을 입력하세요");
            isValid=false;
        }
        else if(!name.equals(confirmed)){
            textInputName.setError("아이디 중복 검사가 필요합니다");
            isValid=false;
        }
        else{
            textInputName.setError(null);
        }
    }

    private void isRestricted(final String email,final String pw,final String nickname){
        Date mDate=new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
        final String date=simpleDateFormat.format(mDate);
        final String converted=email.replace('.','_');
        mRef.child("Restricted").child(converted).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Restricted restricted=dataSnapshot.getValue(Restricted.class);
                    if(restricted.getEndDate()<Integer.parseInt(date)){
                        createUser(email,pw,nickname);
                    }
                    else{
                        showToast("제재일까지는 회원가입이 불가합니다");
                    }
                }
                else{
                    createUser(email,pw,nickname);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createUser(final String email,String pw,final String nickname){
        mAuth.createUserWithEmailAndPassword(email,pw)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User userData=new User(nickname,email);
                            mDatabase.getReference().child("Users").child(nickname).setValue(userData);
                            // success listener 구현해야함
                            FirebaseUser user=mAuth.getCurrentUser();
                            sendEmail(user);
                            updateUI(user);
                        }
                        else{
                            showToast("인터넷 연결 상태/이미 있는 이메일/파이어 베이스 서버 문제");
                            updateUI(null);
                        }
                    }
                });
    }

    private void sendEmail(FirebaseUser user){
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("이메일을 보냈습니다");
                }
                else{
                    showToast("이메일을 보낼 수 없습니다");
                }
            }
        });
    }

    private void updateUI(FirebaseUser user){
        mAuth.signOut();
        if(user!=null) {
            boolean emailVerified = user.isEmailVerified();
            if (!emailVerified) {
                showToast("이메일 인증 후 사용할 수 있습니다");
            }
            Intent intent = new Intent(SignupActivity.this, UniversityActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void chkUser(final String nickname){
        mRef.child("Users").child(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    showToast(nickname+"은 중복된 닉네임입니다");
                }
                else{
                    confirmed=nickname;
                    showToast("사용 가능한 닉네임입니다");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("인터넷 연결을 확인하세요");
            }
        });
    }

    public void showToast(String contents){
        Toast.makeText(SignupActivity.this,contents,Toast.LENGTH_SHORT).show();
    }
}
