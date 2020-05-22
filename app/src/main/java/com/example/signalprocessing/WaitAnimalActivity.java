package com.example.signalprocessing;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WaitAnimalActivity extends AppCompatActivity {

    private RecyclerView rview;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_animal);

        btnAdd=(Button)findViewById(R.id.wait_btn_addwait);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(WaitAnimalActivity.this);
                builder.setTitle("알림").setMessage("대기 동물 명단에 이미 있는 항목인지 확인하세요")
                        .setCancelable(true).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveAddPage();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
    }

    public void moveAddPage(){
        Intent intent=new Intent(WaitAnimalActivity.this,AddWaitAnimalActivity.class);
        startActivity(intent);
        finish();
    }

    public void updateUI(){
        Intent intent=new Intent(WaitAnimalActivity.this,FreeBoardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUI();
    }
}
