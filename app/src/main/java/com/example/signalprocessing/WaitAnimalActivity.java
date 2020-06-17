package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class WaitAnimalActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
    private DatabaseReference mRef=mDatabase.getReference();
    private List<WaitItem> items=new ArrayList<WaitItem>();
    private AllDataAdapter radapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rview;
    private TextView textAll,textAdmit,textReject,textIng;
    private int numAll=0,numAdmit=0,numReject=0,numIng=0;

    private User user;

    private boolean isFabOpen=false;
    private Animation fab_open,fab_close;
    private FloatingActionButton fab,fab1;

    private int thiscp,thisgp;

    ///////////////////////////////////////////////// 네비게이션 바
    /////////////////////////////////////////////////
    private DrawerLayout mDrawerLayout;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private TextView textMypage,textName;
    ///////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_animal);

        user=(User)getIntent().getSerializableExtra("userInfo");
        thiscp=getIntent().getIntExtra("cp",-1);
        thisgp=getIntent().getIntExtra("gp",-1);
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
        func();

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout_name_contest);
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


        // 네비게이션 바
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expandableList = (ExpandableListView) findViewById(R.id.nav_menu);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        // 헤더
        View header=navigationView.getHeaderView(0);

        textName=(TextView)header.findViewById(R.id.navi_name);
        textName.setText(user.getUserName()+"님");

        textMypage=(TextView)header.findViewById(R.id.navi_mypage);
        textMypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),MypageActivity.class);
                intent.putExtra("userInfo",user);
                startActivity(intent);
                finish();
            }
        });

        // 네비게이션 뷰 초기화
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        prepareListData();
        mMenuAdapter = new ExpandableListAdapter(thisgp,thiscp,user.getUserUniv(),this, listDataHeader, listDataChild, expandableList);

        expandableList.setAdapter(mMenuAdapter);
        for(int i=0;i<mMenuAdapter.getGroupCount();i++) {
            expandableList.expandGroup(i);
        }
        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                int cp=(int)mMenuAdapter.getChildId(i,i1);
                int gp=(int)mMenuAdapter.getGroupId(i);
                if(gp==0){
                    if(cp==0){
                        Intent intent=new Intent(getApplicationContext(),FreeBoardActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        startActivity(intent);
                        finish();
                    }
                    else if(cp==1){
//                        Intent intent=new Intent(getApplicationContext(),WaitAnimalActivity.class);
//                        intent.putExtra("userInfo",user);
//                        intent.putExtra("cp",cp);
//                        intent.putExtra("gp",gp);
//                        startActivity(intent);
//                        finish();
                    }
                    else if(cp==2){
                        // 이름 공모전
                    }
                    else{
                        // 동물 도감
                    }
                }
                else{

                }
                return false;
            }
        });

        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return false;
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    private void func() {
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
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    imgView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            size.y * 2 / 3
                    ));

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

    // 네비게이션 바 코드
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    private void prepareListData() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        ExpandedMenuModel item1 = new ExpandedMenuModel();
        item1.setIconName("경북대학교");
        listDataHeader.add(item1);

        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("부산대학교");
        listDataHeader.add(item2);

        List<String> heading1 = new ArrayList<String>();
        heading1.add("자유게시판");
        heading1.add("대기동물 게시판");
        heading1.add("동물 도감");
        heading1.add("이름 공모전");

        listDataChild.put(listDataHeader.get(0), heading1);
        listDataChild.put(listDataHeader.get(1), heading1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            closeDrawer();
        }
        else{
            updateUI();
            finish();
            super.onBackPressed();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
}
