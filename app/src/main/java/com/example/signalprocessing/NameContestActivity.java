package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    ///////////////////////////////////////////////// 네비게이션 바
    /////////////////////////////////////////////////
    private DrawerLayout mDrawerLayout;
    private ExpandableListAdapter mMenuAdapter;
    private ExpandableListView expandableList;
    private List<ExpandedMenuModel> listDataHeader;
    private HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private TextView textMypage,textName;
    private ExpandableListHelper myHelper;
    private NavigationView navigationView;
    ///////////////////////////////////////////////

    private int thiscp,thisgp;
    private String mUniv="";

    private TextView pageTitle;
    private Serializable university=new ArrayList<>();

    private ImageView img_navi;
    private ImageView img_logout;

    private SharedPreferences auto;
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_contest);

        user = (User)getIntent().getSerializableExtra("userInfo");
        thiscp=getIntent().getIntExtra("cp",-1);
        thisgp=getIntent().getIntExtra("gp",-1);
        mUniv=getIntent().getStringExtra("mUniv");
        university=getIntent().getSerializableExtra("university");
        context = this;

        pageTitle=findViewById(R.id.pageTitle);
        pageTitle.setText(mUniv+" 이름 공모전");

        img_navi = (ImageView)findViewById(R.id.img_navi);
        img_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer();
            }
        });

        img_logout = (ImageView)findViewById(R.id.img_logout);
        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auto =getSharedPreferences("autologin", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor=auto.edit();
                editor.clear();
                editor.commit();
                mAuth.signOut();
            }
        });

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

        // 네비게이션 바
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        expandableList = (ExpandableListView) findViewById(R.id.nav_menu);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        myHelper=new ExpandableListHelper((List<String>) university);


        initHeader();
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        prepareListData();
        mMenuAdapter = new ExpandableListAdapter(thisgp,thiscp,user.getUserUniv(),this, listDataHeader, listDataChild, expandableList);
        expandableList.setAdapter(mMenuAdapter);

        openMenuNavi();
        movePageNavi();

        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return false;
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    // 네비게이션 바 코드
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    private void prepareListData() {
        myHelper.initItem();
        listDataChild=myHelper.getListDataChild();
        listDataHeader=myHelper.getListDataHeader();
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

    private void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void initHeader(){
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
    }

    public void movePageNavi(){
        // 클릭 시 이동
        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                int cp=(int)mMenuAdapter.getChildId(i,i1);
                int gp=(int)mMenuAdapter.getGroupId(i);
                ExpandedMenuModel mModel=(ExpandedMenuModel)mMenuAdapter.getGroup(gp);
                String nextUniv=mModel.getIconName();
                Intent intent=null;
                switch (cp){
                    case 0:
                        intent=new Intent(getApplicationContext(),FreeBoardActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",nextUniv);
                        startActivity(intent);
                        finish();
                        break;
                    case 1:
                        intent=new Intent(getApplicationContext(),WaitAnimalActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",nextUniv);
                        startActivity(intent);
                        finish();
                        break;
                    case 2:
                        intent=new Intent(getApplicationContext(),AnimalBookActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",nextUniv);
                        startActivity(intent);
                        finish();
                        break;
                    case 3:
                        if(!nextUniv.equals(mUniv)) {
                            intent = new Intent(getApplicationContext(), NameContestActivity.class);
                            intent.putExtra("userInfo", user);
                            intent.putExtra("cp", cp);
                            intent.putExtra("gp", gp);
                            intent.putExtra("mUniv", nextUniv);
                            startActivity(intent);
                            finish();
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void openMenuNavi(){
        for(int i=0;i<mMenuAdapter.getGroupCount();i++) {
            expandableList.expandGroup(i);
        }
    }

    public void moveMyBoard(){
        Intent intent=new Intent(getApplicationContext(),FreeBoardActivity.class);
        intent.putExtra("userInfo",user);
        intent.putExtra("mUniv",user.getUserUniv());
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            closeDrawer();
        }
        else{
            moveMyBoard();
            finish();
            super.onBackPressed();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

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
                        nameContestViewData.setStatus("심사중");
                    }
                    else if(nameContestData.getStatus().equals("심사완료")) {
                        numAdmit++;
                        nameContestViewData.setStatus("심사완료");
                    }
                    else {
                        numReject++;
                        nameContestViewData.setStatus("거부");
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
}
