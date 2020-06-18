package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MypageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private TextView textName,textUniv,textEmail,textRead;
    private Button btnSend,btnReceive,btnUserBye,btnPW,btnUniv;
    private View thislayout;

    private User user;
    private SharedPreferences auto;

    private RecyclerView rview;
    private MessageAdapter messageAdapter;

    private List<Message> messages=new ArrayList<>();
    private List<String> uidList=new ArrayList<>();
    private boolean isSend=false;

    private int thiscp,thisgp;

    ///////////////////////////////////////////////// 네비게이션 바
    /////////////////////////////////////////////////
    private DrawerLayout mDrawerLayout;
    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private TextView textMypage,textnaviName;
    ///////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        user=(User)getIntent().getSerializableExtra("userInfo");
        thiscp=getIntent().getIntExtra("cp",-1);
        thisgp=getIntent().getIntExtra("gp",-1);

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

        loadMessageInfo(user.getUserName());

        // 회원탈퇴
        btnUserBye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MypageActivity.this);
                builder.setTitle("회원탈퇴 하시겠습니까?").setCancelable(true).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUserDatabase(user.getUserName());
                        updateUI();
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
            }
        });

        // 보낸 쪽지함
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSend=true;
                //loadMessageInfo(user.getUserName());
                messageAdapter.notifyDataSetChanged();
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
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        // 헤더
        View header=navigationView.getHeaderView(0);

        textnaviName=(TextView)header.findViewById(R.id.navi_name);
        textnaviName.setText(user.getUserName()+"님");

        textMypage=(TextView)header.findViewById(R.id.navi_mypage);
//        textMypage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(getApplicationContext(),MypageActivity.class);
//                intent.putExtra("userInfo",user);
//                startActivity(intent);
//                finish();
//            }
//        });

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
                        Intent intent=new Intent(getApplicationContext(),WaitAnimalActivity.class);
                        intent.putExtra("userInfo",user);
                        intent.putExtra("cp",cp);
                        intent.putExtra("gp",gp);
                        startActivity(intent);
                        finish();
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
                uidList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    messages.add(message);
                    uidList.add(snapshot.getKey());
                }
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
        Intent intent=new Intent(MypageActivity.this,FreeBoardActivity.class);
        intent.putExtra("userInfo",user);
        startActivity(intent);
        finish();
    }

    private void sortArray(){
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        Collections.reverse(messages);
    }

    private void deleteMessage(final int position){
        mRef.child("Users").child(user.getUserName()).child("Messages").child(uidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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
        mRef.child("Users").child(user.getUserName()).child("Messages").child(uidList.get(position)).setValue(message);
    }

    class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            sortArray();
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
                    intent.putExtra("uidInfo",uidList.get(i));
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
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
}
