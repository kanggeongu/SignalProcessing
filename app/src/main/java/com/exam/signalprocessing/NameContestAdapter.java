package com.exam.signalprocessing;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NameContestAdapter extends RecyclerView.Adapter<NameContestAdapter.CustomViewHolder> {

    private ArrayList<NameContestViewData> arrayList;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public NameContestAdapter(ArrayList<NameContestViewData> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public NameContestAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.name_contest_item_list, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull NameContestAdapter.CustomViewHolder holder, final int position) {

        Long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String curTime = simpleDateFormat.format(mDate);

        String Time = arrayList.get(position).getTime();
        String startTime = Time.substring(0, 14);
        String endTime = Time.substring(14, 28);

        String status = arrayList.get(position).getStatus();
        if (status.equals("심사완료")) {
            holder.linear1.setBackgroundResource(R.drawable.contest_done);
        }
        else if (status.equals("거부")) {
            holder.linear1.setBackgroundResource(R.drawable.contest_reject);
        }

        /*startTime = startTime.substring(0,4) + "-" + startTime.substring(4,6) + "-" + startTime.substring(6,8) + "-" +
                startTime.substring(8, 10) + "-" + startTime.substring(10, 12);*/
        endTime = endTime.substring(4,6) + "월" + endTime.substring(6,8) + "일 " +
                endTime.substring(8, 10) + ":" + endTime.substring(10, 12);
        holder.textViewTime.setText(endTime);

        int t = arrayList.get(position).getParticipants() == null? 0 : arrayList.get(position).getParticipants().size();
        holder.textViewTheNumberOfParticipants.setText("참여자 수 " + t);
        //holder.textViewOneSentence.setText(arrayList.get(position).getUserName() + " 한마디 -> " + arrayList.get(position).getOneSentence());
        holder.textViewOneSentence.setText(arrayList.get(position).getOneSentence());

        Glide.with(NameContestActivity.context).load(arrayList.get(position).getImage()).into(holder.imageViewnameContest);


        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String NameContestID = arrayList.get(position).getID();
                Intent intent = new Intent(v.getContext(), NameContestIdeaViewHome.class);
                intent.putExtra("contestID", NameContestID);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (arrayList == null ? 0 : arrayList.size());
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        protected LinearLayout linear1;
        protected ImageView imageViewnameContest;
        protected TextView textViewTheNumberOfParticipants;
        protected TextView textViewOneSentence;
        protected TextView textViewTime;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.linear1 = (LinearLayout)itemView.findViewById(R.id.linear1);
            this.imageViewnameContest = (ImageView)itemView.findViewById(R.id.imageViewnameContest);
            this.textViewTheNumberOfParticipants = (TextView)itemView.findViewById(R.id.textViewTheNumberOfParticipants);
            this.textViewOneSentence = (TextView)itemView.findViewById(R.id.textViewOneSentence);
            this.textViewTime = (TextView)itemView.findViewById(R.id.textViewTime);
        }
    }
}
