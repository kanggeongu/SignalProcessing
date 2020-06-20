package com.example.signalprocessing;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CustomViewHolder> {

    private ArrayList<Comment> arrayList;
    private User user;
    private String articleID;
    private String mUniv;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public CommentAdapter(ArrayList<Comment> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public CommentAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        user = ((commentActivity)commentActivity.context).user;
        articleID = ((commentActivity)commentActivity.context).articleID;
        mUniv = ((commentActivity)commentActivity.context).mUniv;

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_comment, parent, false);
        CommentAdapter.CustomViewHolder holder = new CommentAdapter.CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CustomViewHolder holder, int position) {

        Comment comment = arrayList.get(position);

        //유저 닉네임, 댓글 내용
        holder.textViewUserName.setText(comment.getUserID());
        holder.textViewContent.setText(comment.getContent());

        //쪽지
        addMessage(holder);

        //삭제
        addDelete(holder, comment);

        //날짜
        addDate(holder, comment);

        //좋아요
        addLove(holder, comment);

        //댓글
        addCocomment(holder, comment);

        //신고
        addReport(holder, comment);
    }

    //쪽지
    private void addMessage(@NonNull final CommentAdapter.CustomViewHolder holder) {
        holder.buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageDialog sendMessageDialog = new SendMessageDialog(v.getContext(), user.getUserName(), holder.textViewUserName.getText().toString());
                sendMessageDialog.show();
            }
        });
    }

    //삭제
    private void addDelete(@NonNull CommentAdapter.CustomViewHolder holder, final Comment comment) {
        if(holder.textViewUserName.getText().toString().equals(user.getUserName())) {
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("댓글 삭제")
                            .setMessage("삭제 하시겠습니까?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(comment.getCommentID()).removeValue();
                                    Toast.makeText(v.getContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(v.getContext(), "취소하였습니다", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }
            });
        }
        else {
            holder.buttonDelete.setVisibility(View.GONE);
        }
    }

    //날짜
    private void addDate(@NonNull CommentAdapter.CustomViewHolder holder, final Comment comment) {
        String date = comment.getEndDate();
        holder.textViewEndDate.setText(date.substring(4,6) + "월 " + date.substring(6,8) +
                "일 " + date.substring(8,10) + ":" + date.substring(10, 12));
    }

    //좋아요
    private void addLove(@NonNull final CommentAdapter.CustomViewHolder holder, final Comment comment) {
        holder.textViewTheNumberOfLovers.setText(" + " + comment.getLovers().size());

        if(comment.getLovers().contains(user.getUserName())) {
            holder.buttonAddLover.setBackgroundResource(R.drawable.red_fill_heart);
        }
        else {
            holder.buttonAddLover.setBackgroundResource(R.drawable.red_empty_heart);
        }

        holder.buttonAddLover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(comment.getCommentID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Comment comment1 = dataSnapshot.getValue(Comment.class);
                        if (comment1.addLover(user.getUserName())) {
                            holder.buttonAddLover.setBackgroundResource(R.drawable.red_fill_heart);
                            holder.textViewTheNumberOfLovers.setText(" + " + comment1.getLovers().size());
                        }
                        else {
                            holder.buttonAddLover.setBackgroundResource(R.drawable.red_empty_heart);
                            holder.textViewTheNumberOfLovers.setText(" + " + comment1.getLovers().size());
                        }

                        databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(comment.getCommentID()).child("lovers").setValue(comment1.getLovers());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    //답글
    private void addCocomment(@NonNull CommentAdapter.CustomViewHolder holder, final Comment comment) {
        holder.buttonAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CocommentActivity.class);
                intent.putExtra("userInformation", user);
                intent.putExtra("articleInformation", articleID);
                intent.putExtra("commentID", comment.getCommentID());
                v.getContext().startActivity(intent);
            }
        });
    }

    //신고
    private void addReport(@NonNull CommentAdapter.CustomViewHolder holder, final Comment comment) {
        holder.buttonAddReporter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("댓글 신고")
                        .setMessage("해당 댓글을 신고하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(comment.getCommentID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Comment comment1 = dataSnapshot.getValue(Comment.class);
                                        if (comment1.addReporter(user.getUserName())) {
                                            databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(comment.getCommentID()).child("reporters").setValue(comment1.getReporters());
                                            Toast.makeText(v.getContext(), "신고 완료되었습니다", Toast.LENGTH_SHORT).show();

                                            if (comment1.getReporters().size() == 1) {
                                                Long now = System.currentTimeMillis();
                                                RestrictedData restrictedData = new RestrictedData(user.getUserEmail(), comment1.getContent());
                                                databaseReference.child("Restricted").child("Comments").child(Long.toString(now)).setValue(restrictedData);
                                            }
                                        }
                                        else {
                                            Toast.makeText(v.getContext(), "이미 신고한 댓글입니다", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(v.getContext(), "취소하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        protected LinearLayout linearDelete;
        protected TextView textViewUserName, textViewContent, textViewTheNumberOfLovers, textViewEndDate;
        protected Button buttonUser, buttonDelete, buttonAddLover, buttonAddComment, buttonAddReporter;
        protected ImageView imageView;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.linearDelete = (LinearLayout)itemView.findViewById(R.id.linearDelete);

            this.textViewUserName = (TextView)itemView.findViewById(R.id.textViewUserName);
            this.textViewContent = (TextView)itemView.findViewById(R.id.textViewContent);
            this.textViewTheNumberOfLovers = (TextView)itemView.findViewById(R.id.textViewTheNumberOfLovers);
            this.textViewEndDate = (TextView)itemView.findViewById(R.id.textViewEndDate);

            this.buttonUser = (Button)itemView.findViewById(R.id.buttonUser);
            this.buttonDelete = (Button)itemView.findViewById(R.id.buttonDelete);
            this.buttonAddLover = (Button)itemView.findViewById(R.id.buttonAddLover);
            this.buttonAddComment = (Button)itemView.findViewById(R.id.buttonAddComment);
            this.buttonAddReporter = (Button)itemView.findViewById(R.id.buttonAddReporter);

            this.imageView = (ImageView)itemView.findViewById(R.id.imageView);
        }
    }
}
