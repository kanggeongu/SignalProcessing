package com.example.signalprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class FreeBoardActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences auto;
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_board);

        logout=(Button)findViewById(R.id.board_btn_logout);
        logout.setOnClickListener(this);
    }

    private void updateUI(){
        Intent intent=new Intent(FreeBoardActivity.this,MainActivity.class);
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
        switch(view.getId()){
            case R.id.board_btn_logout:
                signOut();
                break;
        }
        updateUI();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
