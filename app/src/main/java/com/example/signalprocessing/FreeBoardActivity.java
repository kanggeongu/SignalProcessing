package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FreeBoardActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences auto;
    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private Button logout,mypage;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_board);

        logout=(Button)findViewById(R.id.board_btn_logout);
        logout.setOnClickListener(this);

        mypage=(Button)findViewById(R.id.board_btn_mypage);
        mypage.setOnClickListener(this);

        mypage.setEnabled(false);
        logout.setEnabled(false);
        loadInfo(mAuth.getCurrentUser().getEmail());
    }

    private void loadInfo(final String email){
        final ProgressDialog pdialog=new ProgressDialog(this);
        pdialog.setTitle("정보를 불러오는 중입니다");
        pdialog.show();
        mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User userData=snapshot.getValue(User.class);
                    if(userData.getUserEmail().equals(email)){
                        user=userData;
                    }
                }
                mypage.setEnabled(true);
                logout.setEnabled(true);
                pdialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateUI(Intent intent){
        startActivity(intent);
        finish();
    }

    private void signOut(){
        auto =getSharedPreferences("autologin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=auto.edit();
        editor.clear();
        editor.commit();
        mAuth.signOut();
    }

    @Override
    public void onClick(View view) {
        Intent intent=null;
        switch(view.getId()){
            case R.id.board_btn_logout:
                signOut();
                intent=new Intent(FreeBoardActivity.this,MainActivity.class);
                break;
            case R.id.board_btn_mypage:
                intent=new Intent(FreeBoardActivity.this,MypageActivity.class);
                intent.putExtra("userInfo",user);
                break;
        }
        updateUI(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
