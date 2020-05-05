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
import com.google.firebase.auth.FirebaseAuth;

public class FindpwActivity extends AppCompatActivity {

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private Button send;
    private EditText editemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpw);

        send=(Button)findViewById(R.id.find_btn_send);
        editemail=(EditText)findViewById(R.id.find_edit_email);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=editemail.getText().toString().trim();
                if(email.equals("")){
                    showToast("이메일을 입력하세요");
                }
                else{
                    sendEmail(email);
                }
            }
        });
    }

    private void sendEmail(String email){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("비밀번호 재설정 메일을 보냈습니다");
                    updateUI();
                }
                else{
                    showToast("이메일이 올바른지 확인하세요");
                }
            }
        });
    }

    public void showToast(String contents){
        Toast.makeText(FindpwActivity.this,contents,Toast.LENGTH_SHORT).show();
    }

    public void updateUI(){
        Intent intent=new Intent(FindpwActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
