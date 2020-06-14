package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaitAnimalActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private List<WaitItem> items=new ArrayList<WaitItem>();
    private AllDataAdapter radapter;

    private RecyclerView rview;
    private TextView textAll,textAdmit,textReject,textIng;
    private int numAll=0,numAdmit=0,numReject=0,numIng=0;

    private User user;

    private boolean isFabOpen=false;
    private Animation fab_open,fab_close;
    private FloatingActionButton fab,fab1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_animal);

        user=(User)getIntent().getSerializableExtra("userInfo");
        Log.e("user","user "+user.getUserName());

        textAll=findViewById(R.id.wait_text_all);
        textAdmit=findViewById(R.id.wait_text_admit);
        textReject=findViewById(R.id.wait_text_reject);
        textIng=findViewById(R.id.wait_text_ing);

        fab_open= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fab_close=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        fab=(FloatingActionButton)findViewById(R.id.mypage_fab);
        fab1 = (FloatingActionButton) findViewById(R.id.mypage_fab1);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);

        rview=(RecyclerView)findViewById(R.id.wait_rview);
        rview.setLayoutManager(new LinearLayoutManager(this));
        radapter=new AllDataAdapter();
        rview.setAdapter(radapter);

        final ProgressDialog pdialog=new ProgressDialog(this);
        pdialog.setTitle("정보를 불러오는 중입니다");
        pdialog.show();

        mRef.child("Waits").child("경북대학교").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                items.clear();
                numAll=numAdmit=numReject=numIng=0;
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    WaitItem waitItem=snapshot.getValue(WaitItem.class);
                    items.add(waitItem);
                    numAll++;
                    if(waitItem.getStatus().equals("심사중")){
                        numIng++;
                    }
                    else if(waitItem.getStatus().equals("거부")){
                        numReject++;
                    }
                    else{
                        numAdmit++;
                    }
                }
                Collections.reverse(items);
                radapter.notifyDataSetChanged();
                textAll.setText(""+numAll);
                textAdmit.setText(""+numAdmit);
                textReject.setText(""+numReject);
                textIng.setText(""+numIng);
                pdialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        Intent intent=null;
        switch(view.getId()){
            case R.id.mypage_fab:
                anim();
                break;
            case R.id.mypage_fab1:
                anim();
                showError();
                break;
        }
    }

    public void anim(){
        if(isFabOpen){
            fab1.startAnimation(fab_close);
            fab1.setClickable(false);
            isFabOpen=false;
        }
        else{
            fab1.startAnimation(fab_open);
            fab1.setClickable(true);
            isFabOpen=true;
        }
    }

    class AllDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waitanimal,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            final WaitItem item=items.get(position);
            ((CustomViewHolder)holder).textname.setText(item.getName());
            Glide.with(WaitAnimalActivity.this).load(item.getPicture()).into(((CustomViewHolder) holder).img);
            (((CustomViewHolder)holder).img).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(WaitAnimalActivity.this);
                    View newView= LayoutInflater.from(WaitAnimalActivity.this).inflate(R.layout.show_image,null,false);
                    builder.setView(newView);
                    final ImageView imgView=(ImageView)newView.findViewById(R.id.showimage_img);
                    final AlertDialog dialog=builder.create();
                    Glide.with(WaitAnimalActivity.this).load(item.getPicture()).into(imgView);
                    dialog.show();
                }
            });
            ((CustomViewHolder)holder).status.setText(item.getStatus());
            ((CustomViewHolder)holder).detail.setText(item.getFeature());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private TextView textname,status,detail;
            private ImageView img;

            public CustomViewHolder(View view) {
                super(view);
                textname=(TextView)view.findViewById(R.id.item_waitanimal_name);
                img=(ImageView)view.findViewById(R.id.item_waitanimal_img);
                status=(TextView)view.findViewById(R.id.item_waitanimal_status);
                detail=(TextView)view.findViewById(R.id.item_waitanimal_detail);
            }
        }
    }

    public void showError(){
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

    public void moveAddPage(){
        Intent intent=new Intent(WaitAnimalActivity.this,AddWaitAnimalActivity.class);
        intent.putExtra("userInfo",user);
        startActivity(intent);
        finish();
    }

    public void updateUI(){
        Intent intent=new Intent(WaitAnimalActivity.this,FreeBoardActivity.class);
        intent.putExtra("userInfo",user);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateUI();
    }
}
