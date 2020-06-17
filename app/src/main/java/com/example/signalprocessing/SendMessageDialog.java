package com.example.signalprocessing;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SendMessageDialog extends Dialog {

    TextView textViewSendUser, textViewReceiveUser, textViewMessageContent;
    Button buttonSendMessage;

    public SendMessageDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_send_message);

    }

    private void initPalette() {
        textViewSendUser = (TextView)findViewById(R.id.textViewSendUser);
        textViewReceiveUser = (TextView)findViewById(R.id.textViewReceiveUser);
        textViewMessageContent = (TextView)findViewById(R.id.textViewMessageContent);

        buttonSendMessage = (Button)findViewById(R.id.buttonSendMessage);
    }
}
