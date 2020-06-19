package com.example.signalprocessing;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NameContestViewIdeaAdapter extends RecyclerView.Adapter<NameContestViewIdeaAdapter.CustomViewHolder> {

    private ArrayList<NameContestIdea> arrayList;
    private User user;
    private String NameContestID;
    private boolean Flag;
    private String mUniv;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public NameContestViewIdeaAdapter(ArrayList<NameContestIdea> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public NameContestViewIdeaAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.name_contest_idea_item_list, parent, false);
        CustomViewHolder holder = new CustomViewHolder(view);

        user = ((NameContestActivity)NameContestActivity.context).user;
        NameContestID = ((NameContestIdeaViewHome)NameContestIdeaViewHome.context).NameContestID;
        mUniv = ((NameContestActivity)NameContestActivity.context).mUniv;

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final NameContestViewIdeaAdapter.CustomViewHolder holder, final int position) {

        holder.textViewTheNumberOfVoters.setText(arrayList.get(position).getVoters().size() + "");
        holder.textViewUserName.setText(arrayList.get(position).getUserName());
        holder.textViewName.setText(arrayList.get(position).getAnimalName());
        holder.textViewReason.setText(arrayList.get(position).getReason());

        if(arrayList.get(position).getVoters().contains(user.getUserName())) {
            holder.buttonCheck.setBackgroundResource(R.drawable.icon_full_box);
            Flag = true;
        }
        else {
            holder.buttonCheck.setBackgroundResource(R.drawable.icon_empty_box);
        }

        holder.buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!user.getUserUniv().equals(mUniv)) {
                    Toast.makeText(v.getContext(), mUniv + " 학생이 아니라서 투표할 수 없습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                Long now = System.currentTimeMillis();
                Date mDate = new Date(now);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String curTime = simpleDateFormat.format(mDate);

                String endTime = ((NameContestIdeaViewHome)NameContestIdeaViewHome.context).endTime;

                if(Long.parseLong(endTime) < Long.parseLong(curTime)) {
                    Toast.makeText(v.getContext(), "기간이 만료된 컨테스트입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Flag && !arrayList.get(position).getVoters().contains(user.getUserName())) {
                    Toast.makeText(v.getContext(), "하나의 항목에만 투표 가능합니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(arrayList.get(position).addVoter(user.getUserName())) {
                    holder.buttonCheck.setBackgroundResource(R.drawable.icon_full_box);
                    Toast.makeText(v.getContext(), "투표하였습니다", Toast.LENGTH_SHORT).show();
                    Flag = true;
                }
                else {
                    holder.buttonCheck.setBackgroundResource(R.drawable.icon_empty_box);
                    Toast.makeText(v.getContext(), "취소하였습니다", Toast.LENGTH_SHORT).show();
                    Flag = false;
                }

                addNameContestVote(NameContestID);
                addNameContestIdeaVote(holder, NameContestID, arrayList.get(position).getID());
            }
        });

        holder.itemView.setTag(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("아이디어 신고")
                        .setMessage("신고 하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(arrayList.get(position).getReporters().contains(user.getUserName())) {
                                    Toast.makeText(v.getContext(), "이미 신고하였습니다", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else {
                                    databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).child("Ideas").child(arrayList.get(position).getID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            NameContestIdea nameContestIdea = dataSnapshot.getValue(NameContestIdea.class);
                                            if (nameContestIdea.addReporter(user.getUserName())) {
                                                databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).child("Ideas").child(arrayList.get(position).getID()).child("reporters").setValue(nameContestIdea.getReporters());

                                                if (nameContestIdea.getReporters().size() >= 10) {
                                                    RestrictedData restrictedData = new RestrictedData(nameContestIdea.getUserName(), nameContestIdea.getAnimalName() + "\n" + nameContestIdea.getReason());
                                                    Long now = System.currentTimeMillis();
                                                    databaseReference.child("Restricted").child("NameContestIdeas").child(Long.toString(now)).setValue(restrictedData);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    Toast.makeText(v.getContext(), "신고 접수 되었습니다", Toast.LENGTH_SHORT).show();
                                }
                                arrayList.get(position).addReporter(user.getUserName());
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(v.getContext(), "취소하였습니다", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return (arrayList != null ? arrayList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        protected TextView textViewName, textViewReason, textViewUserName, textViewTheNumberOfVoters;
        Button buttonCheck;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.textViewTheNumberOfVoters = (TextView)itemView.findViewById(R.id.textViewTheNumberOfVoters);
            this.textViewUserName = (TextView)itemView.findViewById(R.id.textViewUserName);
            this.textViewName = (TextView)itemView.findViewById(R.id.textViewName);
            this.textViewReason = (TextView)itemView.findViewById(R.id.textViewReason);
            this.buttonCheck = (Button)itemView.findViewById(R.id.buttonCheck);
        }
    }

    private void addNameContestVote(final String NameContestID) {
        databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                NameContestData nameContestData = dataSnapshot.getValue(NameContestData.class);
                nameContestData.addVoter(user.getUserName());
                databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).child("voters").setValue(nameContestData.getVoters());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNameContestIdeaVote(final NameContestViewIdeaAdapter.CustomViewHolder holder, final String NameContestID, final String NameContestIdeaID) {
        databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).child("Ideas").child(NameContestIdeaID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                NameContestIdea nameContestIdea = dataSnapshot.getValue(NameContestIdea.class);
                nameContestIdea.addVoter(user.getUserName());
                holder.textViewTheNumberOfVoters.setText(nameContestIdea.getVoters().size() + "");
                databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).child("Ideas").child(NameContestIdeaID).setValue(nameContestIdea);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
