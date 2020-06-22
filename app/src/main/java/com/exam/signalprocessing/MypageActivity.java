package com.exam.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
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

public class MypageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private TextView textName,textUniv,textEmail,textRead;
    private Button btnSend,btnReceive,btnUserBye,btnPW,btnUniv;
    private View thislayout;

    public static Context context;
    public User user;
    private SharedPreferences auto;

    private RecyclerView rview;
    private MessageAdapter messageAdapter;

    private List<Message> messages=new ArrayList<>();
    // private List<String> uidList=new ArrayList<>();
    private boolean isSend=false;

    ///////////////////////////////////////////////// 네비게이션 바
    /////////////////////////////////////////////////
    private DrawerLayout mDrawerLayout;
    private ExpandableListAdapter mMenuAdapter;
    private ExpandableListView expandableList;
    private List<ExpandedMenuModel> listDataHeader;
    private HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private TextView textMypage,textNaviName;
    private ExpandableListHelper myHelper;
    private NavigationView navigationView;
    ///////////////////////////////////////////////
    private int thiscp,thisgp;
    private String mUniv="";
    private List<String> university=new ArrayList<>();

    private ImageView img_navi;
    private ImageView img_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        context = this;

        user=(User)getIntent().getSerializableExtra("userInfo");
        thiscp=getIntent().getIntExtra("cp",-1);
        thisgp=getIntent().getIntExtra("gp",-1);
        mUniv=getIntent().getStringExtra("mUniv");
        university= ((ConnectorActivity)ConnectorActivity.context).university;

        mAuth=FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference();

        textName=(TextView)findViewById(R.id.mypage_textname);
        textEmail=(TextView)findViewById(R.id.mypage_textid);
        textUniv=(TextView)findViewById(R.id.mypage_textuniv);

        btnUniv=findViewById(R.id.mypage_btn_univ);
        btnSend=(Button)findViewById(R.id.mypage_btn_send);
        btnReceive=(Button)findViewById(R.id.mypage_btn_receive);
        btnUserBye=(Button)findViewById(R.id.mypage_btn_bye);
        btnPW=(Button)findViewById(R.id.mypage_btn_editpw);
        thislayout=findViewById(R.id.thislayout);

        rview=(RecyclerView)findViewById(R.id.mypage_rview);
        rview.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter=new MessageAdapter();
        rview.setAdapter(messageAdapter);

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
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                finish();
                startActivity(intent);
            }
        });

        loadMessageInfo(user.getUserName());

        btnUserBye.setEnabled(false);

        // 회원탈퇴
        btnUserBye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AlertDialog.Builder builder=new AlertDialog.Builder(MypageActivity.this);
//                builder.setTitle("회원탈퇴 하시겠습니까?").setCancelable(true).setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        deleteUserDatabase(user.getUserName());
//                        updateUI();
//                    }
//                })
//                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//                AlertDialog dialog=builder.create();
//                dialog.show();
                showToast("서비스 준비중입니다");
                btnUserBye.setEnabled(false);
            }
        });

        // 비밀번호 찾기
        btnPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=textEmail.getText().toString().trim();
                sendEmail(email);
            }
        });

        // 받은 쪽지함
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSend=false;
                //loadMessageInfo(user.getUserName());
                messageAdapter.notifyDataSetChanged();

                btnReceive.setBackgroundResource(R.drawable.yellow_round_rectangle);
                btnReceive.setTextColor(0xFFFFFFFF);

                btnSend.setBackgroundResource(R.drawable.empty_yellow_round_rectangle);
                btnSend.setTextColor(0xFFFFC000);
            }
        });

        // 보낸 쪽지함
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSend=true;
                //loadMessageInfo(user.getUserName());
                messageAdapter.notifyDataSetChanged();

                btnSend.setBackgroundResource(R.drawable.yellow_round_rectangle);
                btnSend.setTextColor(0xFFFFFFFF);

                btnReceive.setBackgroundResource(R.drawable.empty_yellow_round_rectangle);
                btnReceive.setTextColor(0xFFFFC000);
            }
        });

        btnUniv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),UniversityActivity.class);
                intent.putExtra("ischange",true);
                intent.putExtra("userInfo",user);
                startActivity(intent);
                finish();
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

        // openMenuNavi();
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

    private void addItem(Message chatroom){
        mRef.child("Users").child(user.getUserName()).child("Messages").push().setValue(chatroom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("메세지 임시 저장");
                }
                else{
                    showToast("저장 실패");
                }
            }
        });
    }

    private void loadMessageInfo(String userName){
        final ProgressDialog pdialog=new ProgressDialog(this);
        pdialog.setTitle("정보를 불러오는 중입니다");
        pdialog.show();
        mRef.child("Users").child(userName).child("Messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                }
                sortArray();
                showInfo();
                messageAdapter.notifyDataSetChanged();
                pdialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("인터넷 연결을 확인하세요");
            }
        });
    }

    private void deleteUserDatabase(String userName){
        mRef.child("Users").child(userName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                 deleteUser();
            }
        });
    }

    private void deleteUser(){
        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("회원 탈퇴 완료");
                    logOut();
                    updateUI();
                }
            }
        });
    }

    private void logOut(){
        auto =getSharedPreferences("autologin", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=auto.edit();
        editor.clear();
        editor.commit();
        mAuth.signOut();
    }

    public void sendEmail(String email){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showSnackbar("비밀번호 재설정 메일을 보냈습니다");
                }
                else{
                    showToast("인터넷 연결 상태를 확인하세요");
                }
            }
        });
    }

    public void showInfo(){
        textName.setText(user.getUserName());
        String email=user.getUserEmail();
        String emailSource=email.substring(email.lastIndexOf('@')+1);
        textEmail.setText(email);
        if(!emailSource.equals("gmail.com")){
            btnPW.setVisibility(View.VISIBLE);
        }
        textUniv.setText(user.getUserUniv());
    }

    public void showSnackbar(String contents){
        final Snackbar snackbar=Snackbar.make(thislayout,contents,Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("확인", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void showToast(String contents){
        Toast.makeText(MypageActivity.this,contents,Toast.LENGTH_SHORT).show();
    }

    public void updateUI(){
        Intent intent=new Intent(MypageActivity.this,MainActivity.class);
        intent.putExtra("userInfo",user);
        startActivity(intent);
        finish();
    }

    private void sortArray(){
        Collections.reverse(messages);
    }

    private void deleteMessage(final int position){
        mRef.child("Users").child(user.getUserName()).child("Messages").child(Long.toString(messages.get(position).getContentID())).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                messages.remove(position);
                messageAdapter.notifyItemRemoved(position);
                messageAdapter.notifyItemRangeChanged(position,messages.size());
                showToast("삭제 완료");
            }
        });
    }

    private void updateMessage(int position){
        Message message=messages.get(position);
        message.setIsRead("읽음");
        mRef.child("Users").child(user.getUserName()).child("Messages").child(Long.toString(messages.get(position).getContentID())).setValue(message);
    }

    class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int i) {
            final Message message=messages.get(i);
            ((CustomViewHolder)holder).mDate.setText(message.getDate());
            ((CustomViewHolder)holder).mContent.setText(message.getContents());
            ((CustomViewHolder)holder).mRead.setText(message.getIsRead());
            if(isSend==true&&message.getSender().equals(user.getUserName())){
                ((CustomViewHolder)holder).mRead.setVisibility(View.INVISIBLE);
                ((CustomViewHolder)holder).mSender.setText(message.getReceiver());
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            else if(isSend==false&&message.getReceiver().equals(user.getUserName())){
                if(message.getIsRead().equals("읽음")){
                    ((CustomViewHolder)holder).mRead.setVisibility(View.INVISIBLE);
                }
                ((CustomViewHolder)holder).mSender.setText(message.getSender());
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            else{
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
            ((CustomViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateMessage(i);
                    Intent intent = new Intent(MypageActivity.this,ShowMessageActivity.class);
                    intent.putExtra("userInfo",user);
                    intent.putExtra("messageInfo",message);
                    intent.putExtra("uidInfo",messages.get(i).getContentID());
                    startActivity(intent);
                    finish();
                }
            });

            ((CustomViewHolder)holder).itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(MypageActivity.this);
                    builder.setTitle("쪽지를 삭제합니다").setCancelable(true).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(i);
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
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private TextView mSender;
            private TextView mContent;
            private TextView mDate;
            private TextView mRead;

            public CustomViewHolder(View view) {
                super(view);
                mSender=(TextView)view.findViewById(R.id.item_message_name);
                mContent=(TextView)view.findViewById(R.id.item_message_last);
                mDate=(TextView)view.findViewById(R.id.item_message_time);
                mRead=(TextView)view.findViewById(R.id.item_message_isread);
            }
        }
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

        textNaviName=(TextView)header.findViewById(R.id.navi_name);
        textNaviName.setText(user.getUserName()+"님");

        textMypage=(TextView)header.findViewById(R.id.navi_mypage);
    }

    public void movePageNavi(){
        // 클릭 시 이동
        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                int cp=(int)mMenuAdapter.getChildId(i,i1);
                int gp=(int)mMenuAdapter.getGroupId(i);
                ExpandedMenuModel mModel=(ExpandedMenuModel)mMenuAdapter.getGroup(gp);
                String mUniv=mModel.getIconName();
                Intent intent=null;
                switch (cp){
                    case 0:
                        intent=new Intent(getApplicationContext(),FreeBoardActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",mUniv);
                        startActivity(intent);
                        finish();
                        break;
                    case 1:
                        intent=new Intent(getApplicationContext(),WaitAnimalActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",mUniv);
                        startActivity(intent);
                        finish();
                        break;
                    case 2:
                        intent=new Intent(getApplicationContext(),AnimalBookActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",mUniv);
                        startActivity(intent);
                        finish();
                        break;
                    case 3:
                        intent=new Intent(getApplicationContext(),NameContestActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        intent.putExtra("mUniv",mUniv);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("앱을 종료합니다");
            builder.setMessage("정말 종료하시겠습니까?");
            builder.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.setNegativeButton(android.R.string.no,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
}
