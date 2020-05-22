package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VerifyEmailActivity extends AppCompatActivity {

    private Button btnSend;
    private EditText editEmail,editPW;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth=FirebaseAuth.getInstance();

        editPW=(EditText)findViewById(R.id.verify_edit_pw);
        editEmail=(EditText)findViewById(R.id.verify_edit_email);
        btnSend=(Button)findViewById(R.id.verify_btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=editEmail.getText().toString().trim();
                String pw=editPW.getText().toString().trim();
                if(email.equals("")){
                    showToast("이메일을 입력하세요");
                }
                else if(pw.equals("")){
                    showToast("비밀번호를 입력하세요");
                }
                else{
                    verifyAccount(email,pw);
                }
            }
        });
    }

    private void verifyAccount(final String email, String pw) {
        mAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(VerifyEmailActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    sendVerifyEmail(user);
                } else {
                    showToast("회원이 아닙니다");
                }
            }
        });
    }

    private void sendVerifyEmail(FirebaseUser user){
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("이메일을 확인하세요");
                    updateUI();
                }
                else{
                    showToast("인터넷 연결을 확인하세요");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUI();
    }

    public void updateUI(){
        Intent intent=new Intent(VerifyEmailActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void showToast(String contents){
        Toast.makeText(VerifyEmailActivity.this,contents,Toast.LENGTH_SHORT).show();
    }
}
