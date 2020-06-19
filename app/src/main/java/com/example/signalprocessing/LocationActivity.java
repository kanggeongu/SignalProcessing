package com.example.signalprocessing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity {

    private Button addButton;
    private ImageView mapImage;
    private FrameLayout mLayout;
    private LinearLayout mLinear;
    private Context mContext;
    private View Vview;
    LinearLayout ll1,ll2,ll3,ll4,ll5;

    FirebaseDatabase Database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = Database.getReference();

    int isexist;
    int addnum=0;
    int fullnum=0;
    float layoutX=1080,layoutY=1794;
    Typeface tf;

    public String mUniv="";
    public Serializable university=new ArrayList<>();

    //boolean isNew=false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        addButton=(Button)findViewById(R.id.addLButton);
        mapImage=(ImageView)findViewById(R.id.imageMap);
        mLayout=(FrameLayout) findViewById(R.id.mFrame);
        mLinear=(LinearLayout) findViewById(R.id.mLocLinear);
        Vview=(View)findViewById(R.id.Vview);
        mContext=this;

        mUniv=getIntent().getStringExtra("mUniv");
        university=getIntent().getSerializableExtra("universityReturn");

        Intent intent = getIntent();
        final User user=(User)intent.getSerializableExtra("userInfo");
        final String[] downloadurl = new String[1];
        final int[] num = {0};
        final float[] x = new float[1];
        final float[] y = new float[1];
        //tf = getResources().getFont(R.font.bmjua);
        tf = ResourcesCompat.getFont(LocationActivity.this, R.font.bmjua);

        final String animalId=intent.getStringExtra("animalId");
        final List<Location> Loc = new ArrayList<>();
        class NewRunnable implements Runnable {
            NewRunnable() {

            }

            public void run() {
                layoutX=mapImage.getWidth();
                layoutY=mapImage.getHeight();
                mDatabaseReference.child("AnimalBooks").child(mUniv).child(animalId).child("Locations").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Location lc = postSnapshot.getValue(Location.class);
                            Loc.add(lc);
                        }
                        int size = Loc.size();
                        for (int i = 0; i < size; i++) {
                            addView(Loc.get(i).getUserName(), Loc.get(i).getContent(), Loc.get(i).getX() * layoutX + 30, Loc.get(i).getY() * layoutY + 189, i);
                            num[0]++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        final NewRunnable nr = new NewRunnable() ;
        final Thread t = new Thread(nr) ;
        //이미지 불러오기
        mDatabaseReference.child("Universities").child(mUniv).child("mapphoto").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                downloadurl[0] = dataSnapshot.getValue(String.class);
                Glide.with(LocationActivity.this).load(Uri.parse(downloadurl[0])).into(mapImage);
                t.start() ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        initPalette();

        mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x[0] =event.getX();
                y[0] =event.getY();

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(addnum==1) {
                            if (y[0] > layoutY + 189 || y[0] < 189) {
                                Toast.makeText(mContext, "지도 밖으로 벗어났습니다", Toast.LENGTH_LONG).show();
                            } else {
                                if (num[0] < 5) {
                                    addView(user.getUserName(), "", x[0], y[0], num[0]);
                                } else {
                                    addView(user.getUserName(), "", x[0], y[0], fullnum % 5);
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        //추가하기
        final EditText et = new EditText(mContext);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!user.getUserUniv().equals(mUniv)) {
                    Toast.makeText(mContext, mUniv + " 학생이 아니라서 위치 추가를 못 합니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(addnum<=0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("위치 등록").setMessage("위치를 등록하시겠습니까?");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            et.setText("");
                            et.setTextSize(20);
                            et.setHint(" 위치를 입력하세요");
                            et.setPadding(10,0,0,0);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
                            lp.weight=3;
                            lp.setMargins(10,10,10,10);
                            et.setLayoutParams(lp);
                            et.setBackgroundResource(R.drawable.empty_yellow_round_rectangle);
                            et.setTypeface(tf);
                            mLinear.addView(et);

                            LinearLayout.LayoutParams Vv = (LinearLayout.LayoutParams)Vview.getLayoutParams();
                            Vv.weight=0;
                            Vview.setLayoutParams(Vv);
                            LinearLayout.LayoutParams Bb = (LinearLayout.LayoutParams)addButton.getLayoutParams();
                            Bb.weight=1;
                            addButton.setLayoutParams(Bb);

                            addButton.setText("완료");
                            addnum = 1;
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            Toast.makeText(getApplicationContext(), "취소하셨습니다", Toast.LENGTH_SHORT).show();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else{
                    final String content = et.getText().toString();
                    if(content.equals("")){
                        Toast.makeText(LocationActivity.this, "내용을 입력하세요", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(num[0]==5){
                            mDatabaseReference.child("AnimalBooks").child(mUniv).child(animalId).child("Locations").addListenerForSingleValueEvent(new ValueEventListener() {
                                final List<Location> Lt = new ArrayList<>();
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot postSnapshot:dataSnapshot.getChildren()) {
                                        Location loc = postSnapshot.getValue(Location.class);
                                        Lt.add(loc);
                                    }
                                    Lt.remove(0);
                                    Location newloc = new Location();
                                    newloc.setUserName(user.getUserName());
                                    newloc.setContent(content);
                                    newloc.setX((x[0]-30)/layoutX);
                                    newloc.setY((y[0]-189)/layoutY);
                                    Lt.add(newloc);
                                    mDatabaseReference.child("AnimalBooks").child(mUniv).child(animalId).child("Locations").setValue(Lt);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            addView(user.getUserName(),content,x[0],y[0],fullnum%5);
                            fullnum++;
                        }
                        else {
                            num[0]++;
                            mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Location lc = new Location();
                                    lc.setUserName(user.getUserName());
                                    lc.setContent(content);
                                    lc.setX((x[0]-30)/layoutX);
                                    lc.setY((y[0]-194)/layoutY);
                                    mDatabaseReference.child("AnimalBooks").child(mUniv).child(animalId).child("Locations").child(String.valueOf(num[0]-1)).setValue(lc);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            addView(user.getUserName(),content,x[0],y[0],num[0]-1);
                        }
                        mLinear.removeView(et);

                        LinearLayout.LayoutParams Vv = (LinearLayout.LayoutParams)Vview.getLayoutParams();
                        Vv.weight=3;
                        Vview.setLayoutParams(Vv);
                        LinearLayout.LayoutParams Bb = (LinearLayout.LayoutParams)addButton.getLayoutParams();
                        Bb.weight=1;

                        addButton.setText("위치 추가");
                        addnum = -1;
                    }
                }
            }
        });

    }
    void initPalette(){
        ll1 = (LinearLayout)findViewById(R.id.ll1);
        ll2 = (LinearLayout)findViewById(R.id.ll2);
        ll3 = (LinearLayout)findViewById(R.id.ll3);
        ll4 = (LinearLayout)findViewById(R.id.ll4);
        ll5 = (LinearLayout)findViewById(R.id.ll5);
    }
    public void addView(String userId,String content,float x,float y,int n){
        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView textView = new TextView(this);
        textView.setText("작성자 : " + userId);
        textView.setTextSize(10);
        textView.setTypeface(tf);
        textView.setGravity(Gravity.CENTER);

        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        linearLayout.addView(textView);

        textView = new TextView(this);
        textView.setText(content);
        textView.setTextSize(10);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(tf);
        linearLayout.addView(textView);

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_place_yellow);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        linearLayout.addView(imageView);
        switch(n){
            case 0:
                ll1.setX(x-80);
                ll1.setY(y-130);
                ll1.removeAllViews();
                ll1.addView(linearLayout);
                break;
            case 1:
                ll2.setX(x-80);
                ll2.setY(y-130);
                ll2.removeAllViews();
                ll2.addView(linearLayout);
                break;
            case 2:
                ll3.setX(x-80);
                ll3.setY(y-130);
                ll3.removeAllViews();
                ll3.addView(linearLayout);
                break;
            case 3:
                ll4.setX(x-80);
                ll4.setY(y-130);
                ll4.removeAllViews();
                ll4.addView(linearLayout);
                break;
            case 4:
                ll5.setX(x-80);
                ll5.setY(y-130);
                ll5.removeAllViews();
                ll5.addView(linearLayout);
                break;
        }
    }
}

