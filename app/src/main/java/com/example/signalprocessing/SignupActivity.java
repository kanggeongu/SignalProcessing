package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private EditText editemail,editpw,editname;
    private Button esignup,check;
    private String confirmed="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editemail=(EditText)findViewById(R.id.signup_edit_email);
        editpw=(EditText)findViewById(R.id.signup_edit_pw);
        editname=(EditText)findViewById(R.id.signup_edit_name);
        esignup=(Button)findViewById(R.id.signup_btn_signup);
        check=(Button)findViewById(R.id.signup_btn_check);

        esignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=editemail.getText().toString().trim();
                String pw=editpw.getText().toString().trim();
                String nickname=editname.getText().toString().trim();
                if(email.equals("")){
                    showToast("이메일을 입력하세요");
                }
                else if(pw.equals("")){
                    showToast("패스워드를 입력하세요");
                }
                else if(!nickname.equals(confirmed)){
                    showToast("닉네임 중복 검사가 필요합니다");
                }
                else{
                    createUser(email,pw,nickname);
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname=editname.getText().toString().trim();
                if(nickname.equals("")){
                    showToast("닉네임을 입력하세요");
                }
                else {
                    chkUser(nickname);
                }
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
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
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
