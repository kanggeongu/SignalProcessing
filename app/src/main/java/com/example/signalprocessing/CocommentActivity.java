package com.example.signalprocessing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CocommentActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private TextView textViewUserID, textViewCommentContent;
    private EditText editTextWriteCocomment;

    public static Context context;
    public String articleID;
    public User user;
    public String commentID;
    private Comment comment;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewCocomment;

    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Cocomment> arrayList;
    private CocommentAdapter cocommentAdapter;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocomment);

        context = this;
        articleID = (String)getIntent().getSerializableExtra("articleInformation");
        user = (User)getIntent().getSerializableExtra("userInformation");
        commentID = (String)getIntent().getSerializableExtra("commentID");

        initPallete();
        func();

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                func();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void initPallete() {
        textViewUserID = (TextView)findViewById(R.id.textViewUserID);
        textViewCommentContent = (TextView)findViewById(R.id.textViewCommentContent);
        editTextWriteCocomment = (EditText)findViewById(R.id.editTextWriteCocomment);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);

        recyclerViewCocomment = (RecyclerView)findViewById(R.id.recyclerViewCocomment);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewCocomment.setLayoutManager(linearLayoutManager);
    }

    private void func() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("정보를 불러오는 중입니다");
        progressDialog.show();

        arrayList = new ArrayList<>();
        cocommentAdapter = new CocommentAdapter(arrayList);
        recyclerViewCocomment.setAdapter(cocommentAdapter);

        databaseReference.child("Articles").child(user.getUserUniv()).child(articleID).child("Comments").child(commentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comment = dataSnapshot.getValue(Comment.class);
                init(comment.getUserID(), comment.getContent());
                viewCocomments();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                textViewCommentContent.setText("정보를 불러오지 못했습니다.");
            }
        });
    }

    private void init(String userID, String content) {
        textViewUserID.setText(userID);
        textViewCommentContent.setText(content);
    }

    private void viewCocomments() {
        databaseReference.child("Articles").child(user.getUserUniv()).child(articleID).child("Comments").child(commentID).child("Cocomments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Cocomment cocomment = snapshot.getValue(Cocomment.class);
                    arrayList.add(cocomment);
                }

                cocommentAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClickCocomment(View v){
        String textCocomment = editTextWriteCocomment.getText().toString();
        if(textCocomment.equals("")){
            ToastText("내용을 입력해주세요.");
            return;
        }

        editTextWriteCocomment.setText("");

        Long now = System.currentTimeMillis();

        Cocomment cocomment = new Cocomment(Long.toString(now), user.getUserUniv(), user.getUserName(), textCocomment);
        databaseReference.child("Articles").child(user.getUserUniv()).child(articleID).child("Comments").child(commentID).child("Cocomments").child(Long.toString(now)).setValue(cocomment);
        ToastText("작성 완료되었습니다.");
        func();
    }

    private void ToastText(String text) {
        Toast.makeText(CocommentActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
