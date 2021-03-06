package com.exam.signalprocessing;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class commentActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private RecyclerView recyclerViewComment;
    private TextView textViewUserID, textViewArticleContent;
    private EditText editTextWriteComment;
    private ImageView articleimage;

    public String articleID;
    private Article article;
    public String mUniv;

    public User user;
    public static Context context;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;

    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Comment> arrayList;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        context = this;
        articleID = (String)getIntent().getSerializableExtra("articleInformation");
        user = (User)getIntent().getSerializableExtra("userInformation");

        mUniv = ((FreeBoardActivity)FreeBoardActivity.context).mUniv;

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
        textViewArticleContent = (TextView)findViewById(R.id.textViewArticleContent);
        articleimage = (ImageView)findViewById(R.id.artticleimage);
        editTextWriteComment = (EditText)findViewById(R.id.editTextWriteComment);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_layout);

        recyclerViewComment = (RecyclerView)findViewById(R.id.recyclerViewComment);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewComment.setLayoutManager(linearLayoutManager);
    }

    private void func() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("정보를 불러오는 중입니다");
        progressDialog.show();

        arrayList = new ArrayList<>();
        commentAdapter = new CommentAdapter(arrayList);
        recyclerViewComment.setAdapter(commentAdapter);

        databaseReference.child("Articles").child(mUniv).child(articleID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                article = dataSnapshot.getValue(Article.class);
                init(article.getUserID(), article.getContent(), article.getImage());
                viewComments();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                textViewArticleContent.setText("정보를 불러오지 못했습니다.");
            }
        });
    }

    private void init(String userID, String content, String image) {
        textViewUserID.setText(userID);
        textViewArticleContent.setText(content);
        if(image.equals("")){
            articleimage.setVisibility(View.GONE);
        }else{
            Glide.with(commentActivity.this).load(image).into(articleimage);
        }
    }

    private void viewComments() {
        databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    arrayList.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClickComment(View v){
        if (!user.getUserUniv().equals(mUniv)) {
            Toast.makeText(commentActivity.this, mUniv + " 학생이 아니라서 글을 쓸 수 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String textComment = editTextWriteComment.getText().toString();
        if(textComment.equals("")){
            ToastText("내용을 입력해주세요.");
            return;
        }

        editTextWriteComment.setText("");

        Long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.add(Calendar.DATE, 7);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        Comment comment = new Comment(Long.toString(now), user.getUserUniv(), user.getUserName(), textComment, dateFormat.format(c.getTime()));
        databaseReference.child("Articles").child(user.getUserUniv()).child(articleID).child("Comments").child(Long.toString(now)).setValue(comment);
        ToastText("작성 완료되었습니다.");
        func();
    }

    public void onClick(View view) {
        SendMessageDialog sendMessageDialog = new SendMessageDialog(view.getContext(), user.getUserName(), textViewUserID.getText().toString());
        sendMessageDialog.show();
    }

    private void ToastText(String text) {
        Toast.makeText(commentActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
