package com.exam.signalprocessing;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class SearchActivity extends AppCompatActivity {

    private EditText editSearch;
    private Button SearchButton;
    private Context mContext;

    private RecyclerView recyclerView;
    private ArticleAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase Database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = Database.getReference();

    public User user;
    private String mUniv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editSearch=(EditText)findViewById(R.id.editSearch);
        SearchButton=(Button)findViewById(R.id.searchButton);
        recyclerView=(RecyclerView)findViewById(R.id.searchRecycler);
        mContext=this;
        mUniv = ((FreeBoardActivity)FreeBoardActivity.context).mUniv;

        user=(User)getIntent().getSerializableExtra("userInformation");
        final ArrayList<Article> articleList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ArticleAdapter(articleList);

        mDatabaseReference.child("Articles").child(mUniv).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Article article = postSnapshot.getValue(Article.class);
                    articleList.add(article);
                }
                Collections.reverse(articleList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search=editSearch.getText().toString();
                if(search.length()<=1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("").setMessage("2글자 이상 입력해주세요");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else {
                    recyclerView.setAdapter(mAdapter);
                    CharSequence cs=search;
                    mAdapter.getFilter().filter(cs);
                }
            }
        });
    }
}