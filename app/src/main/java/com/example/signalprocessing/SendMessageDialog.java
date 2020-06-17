package com.example.signalprocessing;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SendMessageDialog extends Dialog {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    TextView textViewSendUser, textViewReceiveUser, textViewMessageContent;
    Button buttonSendMessage;

    public SendMessageDialog(@NonNull Context context, String sendUser, String receiveUser) {
        super(context);
        setContentView(R.layout.activity_send_message);

        initPalette();

        textViewSendUser.setText(sendUser);
        textViewReceiveUser.setText(receiveUser);
    }

    private void initPalette() {
        textViewSendUser = (TextView)findViewById(R.id.textViewSendUser);
        textViewReceiveUser = (TextView)findViewById(R.id.textViewReceiveUser);
        textViewMessageContent = (TextView)findViewById(R.id.textViewMessageContent);

        buttonSendMessage = (Button)findViewById(R.id.buttonSendMessage);
        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageContent = textViewMessageContent.getText().toString();
                if (messageContent.equals("")) {
                    Toast.makeText(v.getContext(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                Date mDate=new Date(System.currentTimeMillis());
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM/dd HH:mm");
                String date=simpleDateFormat.format(mDate);

                Message newMessage=new Message(textViewSendUser.getText().toString(), textViewReceiveUser.getText().toString() , date, messageContent);
                addUserMessage(newMessage, v.getContext());

                dismiss();
            }
        });
    }

    private void addUserMessage(final Message myMessage, final Context context){
        Long now = System.currentTimeMillis();
        databaseReference.child("Users").child(textViewSendUser.getText().toString()).child("Messages").child(Long.toString(now)).setValue(myMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    addDestMessage(myMessage, context);
                }
                else{
                    Toast.makeText(context, "전송 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addDestMessage(final Message myMessage, final Context context){
        Long now = System.currentTimeMillis();
        databaseReference.child("Users").child(textViewReceiveUser.getText().toString()).child("Messages").child(Long.toString(now)).setValue(myMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context, "전송 완료", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, "와이파이를 연결해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
