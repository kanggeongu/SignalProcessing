package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class NameContestActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    public User user;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewNameContest;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<NameContestViewData> arrayList;
    private NameContestAdapter nameContestAdapter;

    // 대시보드
    private TextView wait_text_all, wait_text_admit, wait_text_reject, wait_text_ing;
    private int numAll=0,numAdmit=0,numReject=0,numIng=0;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_contest);

        user = (User)getIntent().getSerializableExtra("userInfo");
        context = this;

        initPallete();
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

    private void initPallete() {
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout_name_contest);
        recyclerViewNameContest = (RecyclerView)findViewById(R.id.recyclerViewNameContest);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewNameContest.setLayoutManager(linearLayoutManager);

        wait_text_all = (TextView)findViewById(R.id.wait_text_all);
        wait_text_admit = (TextView)findViewById(R.id.wait_text_admit);
        wait_text_reject = (TextView)findViewById(R.id.wait_text_reject);
        wait_text_ing = (TextView)findViewById(R.id.wait_text_ing);
    }

    private void func() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("정보를 불러오는 중입니다");
        progressDialog.show();

        arrayList = new ArrayList<>();
        nameContestAdapter = new NameContestAdapter(arrayList);
        recyclerViewNameContest.setAdapter(nameContestAdapter);

        databaseReference.child("NameContests").child(user.getUserUniv()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numAll=numAdmit=numReject=numIng=0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NameContestData nameContestData = snapshot.getValue(NameContestData.class);
                    NameContestViewData nameContestViewData = new NameContestViewData(
                            nameContestData.getID(), nameContestData.getImage(),
                            nameContestData.getVoters(), nameContestData.getOneSentence(),
                            nameContestData.getStartTime() + nameContestData.getEndTime(), user.getUserName()
                    );

                    final Long now = System.currentTimeMillis();
                    Date mDate = new Date(now);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    String curTime = simpleDateFormat.format(mDate);
                    if(Long.parseLong(curTime) <= Long.parseLong(nameContestData.getEndTime()) && nameContestData.getStatus().equals("심사중")) {
                        numIng++;
                    }
                    else if(nameContestData.getStatus().equals("심사완료")) {
                        numAdmit++;
                    }
                    else {
                        numReject++;
                    }

                    arrayList.add(nameContestViewData);
                    numAll++;
                }
                Collections.sort(arrayList);
                nameContestAdapter.notifyDataSetChanged();

                wait_text_all.setText("" + numAll);
                wait_text_admit.setText("" + numAdmit);
                wait_text_reject.setText("" + numReject);
                wait_text_ing.setText("" + numIng);

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClickNameContest(View v) {
        Intent intent = new Intent(this, AddNameContestActivity.class);
        intent.putExtra("userInformation", user);
        startActivity(intent);
    }

    public void updateUI(){
        Intent intent=new Intent(NameContestActivity.this,FreeBoardActivity.class);
        intent.putExtra("userInfo",user);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        updateUI();
    }
}
