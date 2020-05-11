package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MypageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser fuser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private TextView textName,textUniv,textEmail;
    private Button btnSend,btnReceive,btnBye,btnPW;
    private View thislayout;

    private User user;
    private SharedPreferences auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        textName=(TextView)findViewById(R.id.mypage_textname);
        textEmail=(TextView)findViewById(R.id.mypage_textid);
        textUniv=(TextView)findViewById(R.id.mypage_textuniv);

        btnSend=(Button)findViewById(R.id.mypage_btn_send);
        btnReceive=(Button)findViewById(R.id.mypage_btn_receive);
        btnBye=(Button)findViewById(R.id.mypage_btn_bye);
        btnPW=(Button)findViewById(R.id.mypage_btn_editpw);

        thislayout=findViewById(R.id.thislayout);

        mAuth=FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference();

        fuser=mAuth.getCurrentUser();

        // 초기 정보 가져오기
        loadInfo(fuser.getEmail());

        // 회원 탈퇴
        btnBye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MypageActivity.this);
                View newView= LayoutInflater.from(MypageActivity.this).inflate(R.layout.item_alertdialog,null,false);
                builder.setView(newView);

                final Button btnOk=(Button)newView.findViewById(R.id.alert_btn_ok);
                final Button btnCancel=(Button)newView.findViewById(R.id.alert_btn_cancel);
                final TextView textMessage=(TextView)newView.findViewById(R.id.alert_message);
                final AlertDialog dialog=builder.create();

                textMessage.setText("회원 탈퇴 시 같은 이메일/닉네임으로 가입할 수 없습니다");
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Bye();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
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

        // 받은
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 보낸
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void loadInfo(final String email){
        final ProgressDialog pdialog=new ProgressDialog(this);
        pdialog.setTitle("정보를 불러오는 중입니다");
        pdialog.show();
        mRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User userData=snapshot.getValue(User.class);
                    if(userData.getUserEmail().equals(email)){
                        user=userData;
                    }
                }
                showInfo();
                pdialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Bye(){
        // 데베에서 지울거니까 자유게시판에서 채팅 보낼때, 유저가 없는 경우에는 못보내게 해야함
        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("회원 탈퇴 완료");
                    logOut();
                    updateUI();
                }
                else{

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
                    showSnackbar("인터넷 연결 상태를 확인하세요");
                }
            }
        });
    }

    public void showInfo(){
        // 이름
        textName.setText(user.getUserName());

        // 이메일 처리
        String email=user.getUserEmail();
        String emailSource=email.substring(email.lastIndexOf('@')+1);
        textEmail.setText(email);
        if(!emailSource.equals("gmail.com")){
            btnPW.setVisibility(View.VISIBLE);
        }

        // 대학
        // textUniv.setText(user.getUniv()); 정보 추가 후 여기 넣자

        // 받은 쪽지
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
        startActivity(intent);
        finish();
    }

    class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
