package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FreeBoardActivity extends AppCompatActivity implements View.OnClickListener{

    //파이어베이스
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    //뷰
    private RecyclerView recyclerViewFreeBoard;
    private SwipeRefreshLayout swipeRefreshLayout;

    //
    public static Context context;
    public User user;
    private Intent intent;

    //리사이클러뷰에 필요한 것들
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Article> arrayList;
    private ArticleAdapter articleAdapter;
    private int thiscp,thisgp;
    private String mUniv="";

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

    private boolean isFabOpen=false;
    private Animation fab_open,fab_close;
    private FloatingActionButton fab,fab1;

    private TextView pageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_board);

        context = this;
        user=(User)getIntent().getSerializableExtra("userInfo");
        thiscp=getIntent().getIntExtra("cp",-1);
        thisgp=getIntent().getIntExtra("gp",-1);
        mUniv=getIntent().getStringExtra("mUniv");

        pageTitle=findViewById(R.id.pageTitle);
        pageTitle.setText(mUniv+" 자유게시판");

        initPalette();
        func();

        fab_open= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fab_close=AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        fab=(FloatingActionButton)findViewById(R.id.mypage_fab);
        fab1 = (FloatingActionButton) findViewById(R.id.mypage_fab1);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);

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
        myHelper=new ExpandableListHelper(this);


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

        databaseReference.child("Articles").child(mUniv).addListenerForSingleValueEvent(new ValueEventListener() {
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

    @Override
    public void onClick(View view) {
        Intent intent=null;
        switch(view.getId()){
            case R.id.mypage_fab:
                anim();
                break;
            case R.id.mypage_fab1:
                anim();
                FreeBoardshowDialog();
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

    public void FreeBoardshowDialog() {
        FreeBoardDialog freeBoardDialog = new FreeBoardDialog(this);
        freeBoardDialog.show();
    }

    class FreeBoardDialog extends Dialog {

        Button buttonWriteArticle1, buttonSearch, buttonComplete;

        public FreeBoardDialog(@NonNull Context context) {
            super(context);
            setContentView(R.layout.free_board_dialog);

            initPalette();
        }

        private void initPalette() {
            buttonWriteArticle1 = (Button)findViewById(R.id.buttonWriteArticle1);
            buttonWriteArticle1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(getApplicationContext(), writeArticleActivity.class);
                    intent.putExtra("userInformation", user);
                    startActivity(intent);
                    dismiss();
                }
            });

            buttonSearch = (Button)findViewById(R.id.buttonSearch);
            buttonSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent = new Intent(getApplicationContext(), SearchActivity.class);
                    intent.putExtra("userInformation", user);
                    startActivity(intent);
                    dismiss();
                }
            });


            buttonComplete = (Button)findViewById(R.id.buttonComplete);
            buttonComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    // 뒤로 가기 할 때 메뉴 액티비티로 이동하게 설정해둠
    public void updateUI(){
        Intent intent=new Intent(getApplicationContext(),ConnectorActivity.class);
        finish();
        startActivity(intent);
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
                        if(!nextUniv.equals(mUniv)) {
                            intent = new Intent(getApplicationContext(), FreeBoardActivity.class);
                            intent.putExtra("userInfo", user);
                            intent.putExtra("cp", cp);
                            intent.putExtra("gp", gp);
                            intent.putExtra("mUniv", nextUniv);
                            startActivity(intent);
                            finish();
                        }
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
                        intent=new Intent(getApplicationContext(),NameContestActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",nextUniv);
                        startActivity(intent);
                        finish();
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

}
