package com.example.signalprocessing;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.CustomViewHolder> implements Filterable {

    private ArrayList<Article> arrayList;
    private ArrayList<Article> unfilteredList;
    private User user;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public ArticleAdapter(ArrayList<Article> arrayList) {
        this.arrayList = arrayList;
        unfilteredList = arrayList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        user = ((FreeBoardActivity)FreeBoardActivity.context).user;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_article, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        Article article = arrayList.get(position);

        //유저 닉네임, 게시글 내용
        holder.textViewUserName.setText(article.getUserID());
        holder.textViewContent.setText(article.getContent());
        Log.e("자유게시판 사진", article.getImage().toString());
        Glide.with(FreeBoardActivity.context).load(article.getImage()).into(holder.imageView);

        //쪽지
        addMessage(holder);

        //삭제
        addDelete(holder, article);

        //날짜
        addDate(holder, article);

        //좋아요
        addLove(holder, article);

        //댓글
        addComment(holder, article);

        //신고
        addReport(holder, article);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) holder.itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        deviceWidth = deviceWidth - 50;
        int deviceHeight = (int) (deviceWidth * 0.6);
        holder.mView.getLayoutParams().width=deviceWidth;
        holder.mView.getLayoutParams().height=deviceHeight;
    }

    //쪽지
    private void addMessage(@NonNull final CustomViewHolder holder) {
        holder.buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SendMessage.class);
                intent.putExtra("sendUser", user.getUserName());
                intent.putExtra("receiveUser", holder.textViewUserName.getText().toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    //삭제
    private void addDelete(@NonNull CustomViewHolder holder, final Article article) {
        if(holder.textViewUserName.getText().toString().equals(user.getUserName())) {
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("게시글 삭제")
                            .setMessage("삭제 하시겠습니까?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseReference.child("Articles").child(user.getUserUniv()).child(article.getArticleID()).removeValue();
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
    private void addDate(@NonNull CustomViewHolder holder, final Article article) {
        holder.textViewEndDate.setText(article.getEndDate());
    }

    //좋아요
    private void addLove(@NonNull final CustomViewHolder holder, final Article article) {
        holder.textViewTheNumberOfLovers.setText(" + " + article.getLovers().size());

        if(article.getLovers().contains(user.getUserName())) {
            holder.buttonAddLover.setBackgroundResource(R.drawable.red_fill_heart);
        }
        else {
            holder.buttonAddLover.setBackgroundResource(R.drawable.red_empty_heart);
        }

        holder.buttonAddLover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Articles").child(user.getUserUniv()).child(article.getArticleID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Article article1 = dataSnapshot.getValue(Article.class);
                        if (article1.addLover(user.getUserName())) {
                            holder.buttonAddLover.setBackgroundResource(R.drawable.red_fill_heart);
                            holder.textViewTheNumberOfLovers.setText(" + " + article1.getLovers().size());
                        }
                        else {
                            holder.buttonAddLover.setBackgroundResource(R.drawable.red_empty_heart);
                            holder.textViewTheNumberOfLovers.setText(" + " + article1.getLovers().size());
                        }

                        databaseReference.child("Articles").child(user.getUserUniv()).child(article.getArticleID()).child("lovers").setValue(article1.getLovers());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    //댓글
    private void addComment(@NonNull CustomViewHolder holder, final Article article) {
        holder.buttonAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), commentActivity.class);
                intent.putExtra("userInformation", user);
                intent.putExtra("articleInformation", article.getArticleID());
                v.getContext().startActivity(intent);
            }
        });
    }

    //신고
    private void addReport(@NonNull CustomViewHolder holder, final Article article) {
        holder.buttonAddReporter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("게시글 신고")
                        .setMessage("해당 게시글을 신고하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference.child("Articles").child(user.getUserUniv()).child(article.getArticleID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Article article1 = dataSnapshot.getValue(Article.class);
                                        if (article1.addReporter(user.getUserName())) {
                                            databaseReference.child("Articles").child(user.getUserUniv()).child(article.getArticleID()).child("reporters").setValue(article1.getReporters());
                                            Toast.makeText(v.getContext(), "신고 완료되었습니다", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(v.getContext(), "이미 신고한 게시글입니다", Toast.LENGTH_SHORT).show();
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if(charString.isEmpty()) {
                    arrayList=unfilteredList;
                } else {
                    ArrayList<Article> filteringList = new ArrayList<>();
                    for(Article item : unfilteredList) {
                        if(item.getContent().toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(item);
                        }
                    }
                    arrayList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = arrayList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                arrayList = (ArrayList<Article>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        protected LinearLayout linearDelete;
        protected TextView textViewUserName, textViewContent, textViewTheNumberOfLovers, textViewEndDate;
        protected Button buttonUser, buttonDelete, buttonAddLover, buttonAddComment, buttonAddReporter;
        protected ImageView imageView;
        protected CardView mView;

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
            this.mView=(CardView)itemView.findViewById(R.id.mArticleView);
        }
    }
}
