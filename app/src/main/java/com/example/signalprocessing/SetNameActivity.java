package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetNameActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private Button enter;
    private EditText editname;
    private User userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_name);

        userData=(User)getIntent().getSerializableExtra("userInformation");

        enter=(Button)findViewById(R.id.initial_btn_enter);
        editname=(EditText)findViewById(R.id.initial_edit_name);

        enter.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.initial_btn_enter:
                addUser();
                break;
        }
    }

    private void addUser(){
        final String nickname=editname.getText().toString().trim();
        if(nickname.equals("")){
            showToast("닉네임을 입력하세요");
        }
        else {
            mRef.child("Users").child(nickname).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        showToast(nickname + "은 중복된 닉네임입니다");
                    } else {
                        userData.setUserName(nickname);
                        mRef.child("Users").child(nickname).setValue(userData);
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showToast("인터넷 연결 문제");
                }
            });
        }
    }

    public void updateUI(FirebaseUser user){
        if(user!=null){
            Intent intent = new Intent(SetNameActivity.this,UniversityActivity.class);
            intent.putExtra("userInfo",userData);
            startActivity(intent);
            finish();
        }
    }

    public void showToast(String contents){
        Toast.makeText(SetNameActivity.this,contents,Toast.LENGTH_SHORT).show();
    }
}
