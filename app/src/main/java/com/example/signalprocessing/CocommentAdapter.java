package com.example.signalprocessing;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class CocommentAdapter extends RecyclerView.Adapter<CocommentAdapter.CustomViewHolder> {

    private ArrayList<Cocomment> arrayList;
    private User user;
    private String articleID;
    private String commentID;
    private String mUniv;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public CocommentAdapter(ArrayList<Cocomment> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public CocommentAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        user = ((CocommentActivity)CocommentActivity.context).user;
        articleID = ((CocommentActivity)CocommentActivity.context).articleID;
        commentID = ((CocommentActivity)CocommentActivity.context).commentID;
        mUniv = ((CocommentActivity)CocommentActivity.context).mUniv;

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_cocomment, parent, false);
        CocommentAdapter.CustomViewHolder holder = new CocommentAdapter.CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CocommentAdapter.CustomViewHolder holder, int position) {

        Cocomment cocomment = arrayList.get(position);

        //유저 닉네임, 답글 내용
        holder.textViewUserName.setText(cocomment.getUserID());
        holder.textViewContent.setText(cocomment.getContent());

        //쪽지
        addMessage(holder);

        //삭제
        addDelete(holder, cocomment);

        //날짜
        addDate(holder, cocomment);

        //좋아요
        addLove(holder, cocomment);

        //신고
        addReport(holder, cocomment);
    }

    //쪽지
    private void addMessage(@NonNull final CocommentAdapter.CustomViewHolder holder) {
        holder.buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageDialog sendMessageDialog = new SendMessageDialog(v.getContext(), user.getUserName(), holder.textViewUserName.getText().toString());
                sendMessageDialog.show();
            }
        });
    }

    //삭제
    private void addDelete(@NonNull CocommentAdapter.CustomViewHolder holder, final Cocomment cocomment) {
        if(holder.textViewUserName.getText().toString().equals(user.getUserName())) {
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("답글 삭제")
                            .setMessage("삭제 하시겠습니까?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(commentID).child("Cocomments").child(cocomment.getCocommentID()).removeValue();
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
            holder.linearDelete.removeAllViews();
        }
    }

    //날짜
    private void addDate(@NonNull CocommentAdapter.CustomViewHolder holder, final Cocomment cocomment) {
        String date = cocomment.getEndDate();
        holder.textViewEndDate.setText(date.substring(4,6) + "월 " + date.substring(6,8) +
                "일 " + date.substring(8,10) + ":" + date.substring(10, 12));
    }

    //좋아요
    private void addLove(@NonNull final CocommentAdapter.CustomViewHolder holder, final Cocomment cocomment) {
        holder.textViewTheNumberOfLovers.setText(" + " + cocomment.getLovers().size());

        if(cocomment.getLovers().contains(user.getUserName())) {
            holder.buttonAddLover.setBackgroundResource(R.drawable.red_fill_heart);
        }
        else {
            holder.buttonAddLover.setBackgroundResource(R.drawable.red_empty_heart);
        }

        holder.buttonAddLover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(commentID).child("Cocomments").child(cocomment.getCocommentID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Cocomment cocomment1 = dataSnapshot.getValue(Cocomment.class);
                        if (cocomment1.addLover(user.getUserName())) {
                            holder.buttonAddLover.setBackgroundResource(R.drawable.red_fill_heart);
                            holder.textViewTheNumberOfLovers.setText(" + " + cocomment1.getLovers().size());
                        }
                        else {
                            holder.buttonAddLover.setBackgroundResource(R.drawable.red_empty_heart);
                            holder.textViewTheNumberOfLovers.setText(" + " + cocomment1.getLovers().size());
                        }

                        databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(commentID).child("Cocomments").child(cocomment.getCocommentID()).child("lovers").setValue(cocomment1.getLovers());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    //신고
    private void addReport(@NonNull CocommentAdapter.CustomViewHolder holder, final Cocomment cocomment) {
        holder.buttonAddReporter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("답글 신고")
                        .setMessage("해당 답글을 신고하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(commentID).child("Cocomments").child(cocomment.getCocommentID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Cocomment cocomment1 = dataSnapshot.getValue(Cocomment.class);
                                        if (cocomment1.addReporter(user.getUserName())) {
                                            databaseReference.child("Articles").child(mUniv).child(articleID).child("Comments").child(commentID).child("Cocomments").child(cocomment.getCocommentID()).child("reporters").setValue(cocomment1.getReporters());
                                            Toast.makeText(v.getContext(), "신고 완료되었습니다", Toast.LENGTH_SHORT).show();

                                            if (cocomment1.getReporters().size() == 1) {
                                                Long now = System.currentTimeMillis();
                                                RestrictedData restrictedData = new RestrictedData(user.getUserEmail(), cocomment1.getContent());
                                                databaseReference.child("Restricted").child("Cocomments").child(Long.toString(now)).setValue(restrictedData);
                                            }
                                        }
                                        else {
                                            Toast.makeText(v.getContext(), "이미 신고한 답글입니다", Toast.LENGTH_SHORT).show();
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
        protected Button buttonUser, buttonDelete, buttonAddLover, buttonAddReporter;

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
            this.buttonAddReporter = (Button)itemView.findViewById(R.id.buttonAddReporter);
        }
    }
}
