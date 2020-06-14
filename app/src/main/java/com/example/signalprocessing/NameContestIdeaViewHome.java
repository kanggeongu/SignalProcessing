package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class NameContestIdeaViewHome extends AppCompatActivity {

    public String NameContestID;
    private TextView textViewContestTime;
    private RecyclerView recyclerViewContestIdea;
    private User user;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private NameContestViewIdeaAdapter nameContestViewIdeaAdapter;
    private ArrayList<NameContestIdea> arrayList;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public static Context context;
    public String startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_contest_idea_view_home);

        NameContestID = (String)getIntent().getSerializableExtra("contestID");
        user = ((NameContestActivity)NameContestActivity.context).user;
        context = this;
        initPallete();
        setTime();
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
        textViewContestTime = (TextView)findViewById(R.id.textViewContestTime);
        recyclerViewContestIdea = (RecyclerView)findViewById(R.id.recyclerViewContestIdea);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout_name_contest);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewContestIdea.setLayoutManager(linearLayoutManager);
    }

    private void setTime() {
        databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                NameContestData nameContestData = dataSnapshot.getValue(NameContestData.class);
                startTime = nameContestData.getStartTime();
                String startTime1 = startTime.substring(0,4) + "-" + startTime.substring(4,6) + "-" + startTime.substring(6,8) + "-" +
                        startTime.substring(8, 10) + "-" + startTime.substring(10, 12) + "-" + startTime.substring(12, 14);

                endTime = nameContestData.getEndTime();
                String endTime1 = endTime.substring(0,4) + "-" + endTime.substring(4,6) + "-" + endTime.substring(6,8) + "-" +
                        endTime.substring(8, 10) + "-" + endTime.substring(10, 12) + "-" + endTime.substring(12, 14);

                textViewContestTime.setText(startTime1 + " ~\n\t" + endTime1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void func() {

        arrayList = new ArrayList<>();
        nameContestViewIdeaAdapter = new NameContestViewIdeaAdapter(arrayList);
        recyclerViewContestIdea.setAdapter(nameContestViewIdeaAdapter);

        databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).child("Ideas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    NameContestIdea nameContestIdea = snapshot.getValue(NameContestIdea.class);
                    arrayList.add(nameContestIdea);
                }
                Collections.sort(arrayList);
                nameContestViewIdeaAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClickAddContestIdea(View v) {
        Long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String curTime = simpleDateFormat.format(mDate);



        if(Long.parseLong(endTime) < Long.parseLong(curTime)) {
            Toast.makeText(this, "기간이 만료된 컨테스트입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, NameContestAddContestIdea.class);
        intent.putExtra("contestID", NameContestID);
        intent.putExtra("userInformation", user);
        startActivity(intent);
    }
}
