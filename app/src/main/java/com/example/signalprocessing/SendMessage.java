package com.example.signalprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class SendMessage extends AppCompatActivity {

    private TextView textViewSendUser, textViewReceiveUser;
    private String SendUserName, ReceiveUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        initPallete();
        SendUserName = (String)getIntent().getSerializableExtra("sendUser");
        ReceiveUserName = (String)getIntent().getSerializableExtra("receiveUser");
        textViewSendUser.setText(SendUserName);
        textViewReceiveUser.setText(ReceiveUserName);
    }

    private void initPallete() {
        textViewSendUser = (TextView)findViewById(R.id.textViewSendUser);
        textViewReceiveUser = (TextView)findViewById(R.id.textViewReceiveUser);
    }
}
