package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Collections;

public class FreeBoardActivity extends AppCompatActivity{

    //파이어베이스
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    //뷰
    private RecyclerView recyclerViewFreeBoard;
    private SwipeRefreshLayout swipeRefreshLayout;

    //
    public static Context context;
    public User user;

    //리사이클러뷰에 필요한 것들
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Article> arrayList;
    private ArticleAdapter articleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_board);

        context = this;
        user=(User)getIntent().getSerializableExtra("userInfo");
        initPalette();
        func();

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                func();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void initPalette() {
        recyclerViewFreeBoard = (RecyclerView)findViewById(R.id.recyclerViewFreeBoard);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewFreeBoard.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);
    }

    private void func() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("정보를 불러오는 중입니다");
        progressDialog.show();

        arrayList = new ArrayList<>();
        articleAdapter = new ArticleAdapter(arrayList);
        recyclerViewFreeBoard.setAdapter(articleAdapter);

        databaseReference.child("Articles").child(user.getUserUniv()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Article article = snapshot.getValue(Article.class);
                    arrayList.add(article);
                }

                Collections.reverse(arrayList);
                articleAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonPopUp:
                Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
                intent.putExtra("userInformation", user);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUI();
    }

    // 뒤로 가기 할 때 메뉴 액티비티로 이동하게 설정해둠
    public void updateUI(){
        Intent intent=new Intent(getApplicationContext(),ConnectorActivity.class);
        finish();
        startActivity(intent);
    }
}
