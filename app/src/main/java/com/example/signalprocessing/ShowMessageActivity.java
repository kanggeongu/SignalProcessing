package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowMessageActivity extends AppCompatActivity {

    private TextView name,contents,category;
    private EditText Rcontents;
    private Button btnReply,btnSend,btnReport;
    private User user;
    private Message message;
    private boolean isSend=false;
    private String destUser="";
    private String myUid;
    private int errorCode=0;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);

        mDatabase=FirebaseDatabase.getInstance();
        mRef=mDatabase.getReference();

        name=(TextView)findViewById(R.id.showmessage_text_name);
        contents=(TextView)findViewById(R.id.showmessage_text_contents);
        category=(TextView)findViewById(R.id.showmessage_text_category);
        Rcontents=(EditText)findViewById(R.id.showmessage_edit_contents);
        btnReply=(Button)findViewById(R.id.showmessage_btn_reply);
        btnSend=(Button)findViewById(R.id.showmessage_btn_send);
        btnReport=(Button)findViewById(R.id.showmessage_btn_report);

        // 오류 방지 - 이거 이상해
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        user= (User)getIntent().getSerializableExtra("userInfo");
        message=(Message)getIntent().getSerializableExtra("messageInfo");
        myUid=(String)getIntent().getSerializableExtra("uidInfo");

        if(message.getReceiver().equals(user.getUserName())){
            destUser=message.getSender();
            category.setText("받은 메세지");
            name.setText("보낸 사람 : "+destUser);
        }
        else{
            destUser=message.getReceiver();
            category.setText("보낸 메세지");
            name.setText("받는 사람 : "+destUser);
            isSend=true;
        }

        if(message.isReported()==true){
            btnReport.setText("신고 접수가 완료되었습니다");
            btnReport.setEnabled(false);
        }

        contents.setText(message.getContents());
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnReply.setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
                Rcontents.setVisibility(View.VISIBLE);
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 넣자
                updateMessage();
                btnReport.setText("신고 접수가 완료되었습니다");
                btnReport.setEnabled(false);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyContents=Rcontents.getText().toString().trim();
                if(replyContents.equals("")){
                    showToast("전송할 내용을 입력하세요");
                }
                else{
                    btnSend.setEnabled(false);
                    Date mDate=new Date(System.currentTimeMillis());
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM/dd HH:mm");
                    String date=simpleDateFormat.format(mDate);
                    Message newMessage=new Message(user.getUserName(),destUser,date,replyContents);
                    addUserMessage(newMessage);
                }
            }
        });
    }

    private void updateMessage(){
        message.setReported(true);
        mRef.child("Users").child(user.getUserName()).child("Messages").child(myUid).setValue(message);
    }

    public void addUserMessage(final Message myMessage){
        mRef.child("Users").child(user.getUserName()).child("Messages").push().setValue(myMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    addDestMessage(myMessage);
                }
                else{
                    showToast("전송 실패");
                    btnSend.setEnabled(true);
                }
            }
        });
    }

    public void addDestMessage(final Message myMessage){
        mRef.child("Users").child(destUser).child("Messages").push().setValue(myMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showToast("전송 완료");
                    updateUI();
                }
                else{
                    showToast("와이파이를 연결해주세요");
                    btnSend.setEnabled(true);
                }
            }
        });
    }

    public void showToast(String toastMessage){
        Toast.makeText(ShowMessageActivity.this,toastMessage,Toast.LENGTH_SHORT).show();
    }

    public void updateUI(){
        Intent intent = new Intent(ShowMessageActivity.this,MypageActivity.class);
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