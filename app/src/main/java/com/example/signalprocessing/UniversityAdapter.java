package com.example.signalprocessing;

import android.app.Activity;
import android.content.Context;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UniversityAdapter extends RecyclerView.Adapter<UniversityAdapter.ViewHolder> implements Filterable {
    private List<Universityitem> mDataset;
    private List<Universityitem> unfilteredList;
    private Context mContext;
    private String userId;
    private Boolean ischange;
    private User user;

    private FirebaseDatabase Database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseReference = Database.getReference();

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if(charString.isEmpty()) {
                    mDataset=unfilteredList;
                } else {
                    ArrayList<Universityitem> filteringList = new ArrayList<>();
                    for(Universityitem item : unfilteredList) {
                        if(item.getUniversityName().toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(item);
                        }
                    }
                    mDataset = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mDataset;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDataset = (ArrayList<Universityitem>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView Image_uni;
        TextView Name_uni;
        TextView LikeNum;
        Button LikeButton;
        CardView mView;

        private ViewHolder(@NonNull View v) {
            super(v);

            Image_uni= v.findViewById(R.id.universityImage);
            Name_uni=v.findViewById(R.id.universityName);
            LikeNum=v.findViewById(R.id.LikeNum);
            LikeButton=v.findViewById(R.id.LikeButton);
            mView=v.findViewById(R.id.uvItemView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)

    public UniversityAdapter(List<Universityitem> myDataset, Context context, User user, Boolean ischange) {
        mDataset = myDataset;
        unfilteredList = myDataset;
        mContext = context;
        this.user=user;
        userId=user.getUserName();
        this.ischange = ischange;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public UniversityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_university, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Universityitem university = mDataset.get(position);
        holder.Name_uni.setText(university.getUniversityName());
        Glide.with(mContext).load(university.getPhoto()).into(holder.Image_uni);
        holder.LikeNum.setText(String.valueOf(university.getFollowers()));
        //아이템 레이아웃 크기 변경
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) holder.itemView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        deviceWidth = deviceWidth / 2 - 40;
        int deviceHeight = (int) (deviceWidth * 1.25);
        holder.mView.getLayoutParams().width=deviceWidth;
        holder.mView.getLayoutParams().height=deviceHeight;
        holder.Name_uni.getLayoutParams().width=deviceWidth/2;
        holder.LikeButton.getLayoutParams().width=deviceWidth/4-20;
        holder.LikeNum.getLayoutParams().width=deviceWidth/4;
        holder.mView.requestLayout();

        final int[] follow = {university.getFollowers()};
        if(follow[0]>=100){
            holder.LikeNum.setTextSize(20);
        }
        if(user.getUserUniv().equals(university.getUniversityName())) {
            holder.LikeButton.setBackgroundResource(R.drawable.red_fill_heart);
        }

        holder.LikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);

                        Log.v("university", user.getUserUniv());
                        if (user.getUserUniv().equals("")) {
                            holder.LikeButton.setBackgroundResource(R.drawable.red_fill_heart);
                            follow[0]++;
                            holder.LikeNum.setText(String.valueOf(follow[0]));
                            user.setUserUniv(university.getUniversityName());
                            mDatabaseReference.child("Users").child(userId).child("userUniv").setValue(university.getUniversityName());
                            mDatabaseReference.child("Universities").child(university.getUniversityName()).child("followers").setValue(follow[0]);

                            Log.v("university", "ischange -> " + ischange);
                            if(ischange){
                                Intent intent = new Intent(mContext, MypageActivity.class);
                                intent.putExtra("userInfo",user);
                                mContext.startActivity(intent);
                                ((Activity)mContext).finish();
                            }
                            else {
                                Intent intent = new Intent(mContext, MainActivity.class);
                                mContext.startActivity(intent);
                                ((Activity) mContext).finish();
                            }
                        } else if (user.getUserUniv().equals(university.getUniversityName())) {
                            Log.v("university", "else if");
                            holder.LikeButton.setBackgroundResource(R.drawable.red_empty_heart);
                            follow[0]--;
                            holder.LikeNum.setText(String.valueOf(follow[0]));
                            mDatabaseReference.child("Users").child(userId).child("userUniv").setValue("");
                            mDatabaseReference.child("Universities").child(university.getUniversityName()).child("followers").setValue(follow[0]);
                        } else {
                            Log.v("university", "else");
                            Toast.makeText(mContext, "이미 선택하셨습니다", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}
