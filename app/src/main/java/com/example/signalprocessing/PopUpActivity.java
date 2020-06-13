package com.example.signalprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PopUpActivity extends AppCompatActivity {

    private TextView txtText;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up);

        initPalette();
        user = (User)getIntent().getSerializableExtra("userInformation");
    }

    private void initPalette() {

    }

    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()){
            case R.id.buttonComplete:

                break;
            case R.id.buttonWriteArticle1:
                intent = new Intent(getApplicationContext(), writeArticleActivity.class);
                intent.putExtra("userInformation", user);
                startActivity(intent);
                break;
            case R.id.buttonSearch:
                intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.putExtra("userInformation", user);
                startActivity(intent);
                break;
        }

        finish();
    }
}
