package com.example.signalprocessing;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AnimalBookActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private List<AnimalBook> AnimalBooks = new ArrayList<>();
    private User user;
    private GridLayoutManager layoutManager;
    private MyAdapter myAdapter;
    private AnimalBook animalBook3;

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
        setContentView(R.layout.activity_animal_book);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        user = (User)getIntent().getSerializableExtra("userInfo");

        thiscp=getIntent().getIntExtra("cp",-1);
        thisgp=getIntent().getIntExtra("gp",-1);
        mUniv=getIntent().getStringExtra("mUniv");
        university=getIntent().getSerializableExtra("university");

        pageTitle=findViewById(R.id.pageTitle);
        pageTitle.setText(mUniv+" 동물 도감");

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

        databaseReference.child("AnimalBooks").child(mUniv).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AnimalBooks.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    AnimalBook animalBook = snapshot.getValue(AnimalBook.class);

                    AnimalBooks.add(animalBook);
                }
                AnimalBooks.sort(new Comparator<AnimalBook>() {
                    @Override
                    public int compare(AnimalBook t1, AnimalBook t2) {
                        int like1 = t1.getLiker().size();
                        int like2 = t2.getLiker().size();

                        if(like1 == like2) return 0;
                        else if(like1 < like2) return 1;
                        else return -1;
                    }
                });
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG: ", "Failed to read value", databaseError.toException());
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
                intent.putExtra("university",university);
                intent.putExtra("mUniv",mUniv);
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
                        intent.putExtra("university",university);
                        startActivity(intent);
                        finish();
                        break;
                    case 1:
                        intent=new Intent(getApplicationContext(),WaitAnimalActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",nextUniv);
                        intent.putExtra("university",university);
                        startActivity(intent);
                        finish();
                        break;
                    case 2:
                        if(!nextUniv.equals(mUniv)) {
                            intent = new Intent(getApplicationContext(), AnimalBookActivity.class);
                            intent.putExtra("userInfo", user);
                            intent.putExtra("cp", cp);
                            intent.putExtra("gp", gp);
                            intent.putExtra("mUniv", nextUniv);
                            intent.putExtra("university",university);
                            startActivity(intent);
                            finish();
                        }
                        break;
                    case 3:
                        intent=new Intent(getApplicationContext(),NameContestActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",nextUniv);
                        intent.putExtra("university",university);
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
        intent.putExtra("university",university);
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

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.animalbook_item_list, parent, false);
            return new CustomViewHolder(view);
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView mean;
            TextView location;
            TextView gender;
            ArrayList<Content> content;
            ImageView animal;
            Button like;
            TextView likes;
            Button loc;

            CustomViewHolder(View view) {
                super(view);

                animal = view.findViewById(R.id.animal);
                name = view.findViewById(R.id.name);
                mean = view.findViewById(R.id.mean);
                location = view.findViewById(R.id.location);
                gender = view.findViewById(R.id.gender);
                content = new ArrayList<>();
                likes = view.findViewById(R.id.likes);
                like = view.findViewById(R.id.like);
                loc = view.findViewById(R.id.loc);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            final Intent intent = new Intent(AnimalBookActivity.this, AnimalBookDetailActivity.class);
                            intent.putExtra("userInfo", getIntent().getSerializableExtra("userInfo"));
                            intent.putExtra("animalID", AnimalBooks.get(pos).getAnimalID());
                            intent.putExtra("사진", AnimalBooks.get(pos).getImage());
                            intent.putExtra("university",university);
                            intent.putExtra("mUniv",mUniv);
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((CustomViewHolder)holder).name.setText(AnimalBooks.get(position).getName());
            ((CustomViewHolder)holder).gender.setText(AnimalBooks.get(position).getGender());
            ((CustomViewHolder)holder).mean.setText(AnimalBooks.get(position).getMean());
            ((CustomViewHolder)holder).location.setText(AnimalBooks.get(position).getLocation());
            Glide.with(AnimalBookActivity.this).load(AnimalBooks.get(position).getImage()).into(((CustomViewHolder) holder).animal);

            final String animalID = AnimalBooks.get(position).getAnimalID();
            //위치 인텐트 넘어가는 곳
            ((CustomViewHolder)holder).loc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(AnimalBookActivity.this,LocationActivity.class);
                    intent.putExtra("userInfo",user);
                    intent.putExtra("university",""/*동물 도감 대학 정보 넘겨주기*/);
                    intent.putExtra("animalId",animalID);
                    intent.putExtra("universityReturn",university);
                    intent.putExtra("mUniv",mUniv);
                    startActivity(intent);
                }
            });


            databaseReference.child("Users").child(user.getUserName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    ((CustomViewHolder)holder).likes.setText(String.valueOf(AnimalBooks.get(position).getLiker().size()));

                    if(AnimalBooks.get(position).getLiker().contains(user.getUserName())){
                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.filled);
                    }else {
                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.empty);
                    }

                    ((CustomViewHolder)holder).like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    animalBook3 = dataSnapshot.getValue(AnimalBook.class);
                                    assert animalBook3 != null;
                                    if (animalBook3.addLiker(user.getUserName())) {
                                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.filled);
                                        ((CustomViewHolder) holder).likes.setText(""+animalBook3.getLiker().size());
                                    } else {
                                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.empty);
                                        ((CustomViewHolder) holder).likes.setText("" + animalBook3.getLiker().size());
                                    }

                                    databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).child("liker").setValue(animalBook3.getLiker());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

        @Override
        public int getItemCount() {
            return AnimalBooks.size();
        }
    }

}