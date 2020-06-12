package com.example.signalprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class writeArticleActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private EditText editTextContent;
    private Button buttonAddArticle;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_article);

        initPalette();
    }

    private void initPalette() {
        editTextContent = (EditText)findViewById(R.id.editTextContent);
        buttonAddArticle = (Button)findViewById(R.id.buttonAddArticle);
        user = (User)getIntent().getSerializableExtra("userInformation");
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.buttonAddArticle:
                String content = editTextContent.getText().toString();
                if(content.equals("")){
                    Toast.makeText(getApplicationContext(),"내용이 비어있습니다.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                final Long now = System.currentTimeMillis();

                Article article = new Article(Long.toString(now), user.getUserUniv(), user.getUserName(), content);
                databaseReference.child("Articles").child(user.getUserUniv()).child(Long.toString(now)).setValue(article);
                break;
        }
    }
}
