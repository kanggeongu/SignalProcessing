package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConnectorActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences auto;
    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private Button logout,mypage,waitanimal,freeboard, namecontest, animalbook;
    private User user;

    private int initGroup=0,initItem=0;
    private int fromConnector=0;
    public static List<String> university=new ArrayList<>();
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector);

        context=this;

        /*
        logout=(Button)findViewById(R.id.board_btn_logout);
        logout.setOnClickListener(this);

        mypage=(Button)findViewById(R.id.board_btn_mypage);
        mypage.setOnClickListener(this);

        waitanimal=(Button)findViewById(R.id.board_btn_waitanimal);
        waitanimal.setOnClickListener(this);

        freeboard=(Button)findViewById(R.id.board_btn_board);
        freeboard.setOnClickListener(this);

        namecontest = (Button)findViewById(R.id.board_btn_namecontest);
        namecontest.setOnClickListener(this);

        animalbook = (Button)findViewById(R.id.board_btn_animalbook);
        animalbook.setOnClickListener(this); */

        loadInfo(mAuth.getCurrentUser().getEmail());
    }

    public void initUniversity(){
        mRef.child("Universities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                university.clear();
                int index=0;
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    String name=snapshot.getKey();
                    university.add(name);
                    if(name.equals(user.getUserUniv())){
                        initGroup=index;
                        initItem=0;
                    }
                    index+=1;
                }
                goFreeBoard();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goFreeBoard() {
        Intent intent=new Intent(getApplicationContext(),FreeBoardActivity.class);
        intent.putExtra("mUniv",user.getUserUniv());
        intent.putExtra("userInfo",user);
        // intent.putExtra("university", (Serializable) university);
        intent.putExtra("gp",initGroup);
        intent.putExtra("from",100);
        intent.putExtra("cp",initItem);
        startActivity(intent);
        finish();
    }

    private void checkRestricted(String userEmail) {
        String email = userEmail.replace('.', '_');
        mRef.child("Restricted").child("Users").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Restricted restricted = dataSnapshot.getValue(Restricted.class);
                if (restricted == null) {
                    initUniversity();
                }
                else {
                    Date mDate=new Date(System.currentTimeMillis());
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
                    String date=simpleDateFormat.format(mDate);

                    if(restricted.getEndDate() < Integer.parseInt(date)){
                        initUniversity();
                    }
                    else{
                        int year = restricted.getEndDate() / 10000;
                        int month = (restricted.getEndDate() % 10000) / 100;
                        int day = restricted.getEndDate() % 100;

                        AlertDialog.Builder builder = new AlertDialog.Builder(ConnectorActivity.this);
                        builder.setCancelable(false);
                        builder.setTitle("앱 사용 금지")
                                .setMessage(year + "년 " + month + "월 " + day + "일까지 사용 중지됩니다")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                        builder.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ConnectorActivity.this, "와이파이를 확인해주세요", Toast.LENGTH_SHORT).show();
            }
        });
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

                        checkRestricted(user.getUserEmail());
                    }
                }
                pdialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ConnectorActivity.this, "와이파이 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
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
        /*Intent intent=null;
        switch(view.getId()){
            case R.id.board_btn_logout:
                signOut();
                intent=new Intent(getApplicationContext(),MainActivity.class);
                break;
            case R.id.board_btn_mypage:
                intent=new Intent(getApplicationContext(),MypageActivity.class);
                intent.putExtra("userInfo",user);
                break;
            case R.id.board_btn_waitanimal:
                intent=new Intent(getApplicationContext(),WaitAnimalActivity.class);
                intent.putExtra("userInfo",user);
                intent.putExtra("cp",1);
                intent.putExtra("gp",0);
                break;
            case R.id.board_btn_board:
                intent=new Intent(getApplicationContext(),FreeBoardActivity.class);
                intent.putExtra("cp",0);
                intent.putExtra("gp",0);
                intent.putExtra("userInfo",user);
                break;
            case R.id.board_btn_namecontest:
                intent = new Intent(getApplicationContext(), NameContestActivity.class);
                intent.putExtra("userInfo",user);

                break;
            case R.id.board_btn_animalbook:
                intent = new Intent(getApplicationContext(), AnimalBookActivity.class);
                intent.putExtra("userInfo", user);
                break;
        }
        updateUI(intent);*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
