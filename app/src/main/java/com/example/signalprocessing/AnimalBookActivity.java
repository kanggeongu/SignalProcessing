package com.example.signalprocessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AnimalBookActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private List<AnimalBook> AnimalBooks = new ArrayList<>();
    private User user;
    private GridLayoutManager layoutManager;
    private MyAdapter myAdapter;
    private AnimalBook animalBook3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_book);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        user = (User)getIntent().getSerializableExtra("userInfo");



        databaseReference.child("AnimalBooks").child("경북대학교").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AnimalBooks.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    AnimalBook animalBook = snapshot.getValue(AnimalBook.class);

                    AnimalBooks.add(animalBook);
                }
                AnimalBooks.sort(new Comparator<AnimalBook>() {
                    @Override
                    public int compare(AnimalBook t1, AnimalBook t2) {
                        int like1 = t1.getLiker().size();
                        int like2 = t2.getLiker().size();

                        if(like1 == like2) return 0;
                        else if(like1 < like2) return 1;
                        else return -1;
                    }
                });
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG: ", "Failed to read value", databaseError.toException());
            }
        });

    }

    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.animalbook_item_list, parent, false);
            return new CustomViewHolder(view);
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView mean;
            TextView location;
            TextView gender;
            ArrayList<Content> content;
            ImageView animal;
            Button like;
            TextView likes;

            CustomViewHolder(View view) {
                super(view);

                animal = view.findViewById(R.id.animal);
                name = view.findViewById(R.id.name);
                mean = view.findViewById(R.id.mean);
                location = view.findViewById(R.id.location);
                gender = view.findViewById(R.id.gender);
                content = new ArrayList<>();
                likes = view.findViewById(R.id.likes);
                like = view.findViewById(R.id.like);


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION){
                            final Intent intent = new Intent(AnimalBookActivity.this, AnimalBookDetailActivity.class);
                            intent.putExtra("이름", name.getText().toString());
                            intent.putExtra("뜻", mean.getText().toString());
                            intent.putExtra("위치", location.getText().toString());
                            intent.putExtra("성별", gender.getText().toString());
                            intent.putExtra("userInfo", getIntent().getSerializableExtra("userInfo"));
                            intent.putExtra("animalID", AnimalBooks.get(pos).getAnimalID());
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            Bitmap bitmap = ((BitmapDrawable)animal.getDrawable()).getBitmap();
                            float scale = (1024/(float)bitmap.getWidth());
                            int image_w = (int) (bitmap.getWidth() * scale);
                            int image_h = (int) (bitmap.getHeight() * scale);
                            Bitmap resize = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
                            resize.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            intent.putExtra("사진", byteArray);
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((CustomViewHolder)holder).name.setText(AnimalBooks.get(position).getName());
            ((CustomViewHolder)holder).gender.setText(AnimalBooks.get(position).getGender());
            ((CustomViewHolder)holder).mean.setText(AnimalBooks.get(position).getMean());
            ((CustomViewHolder)holder).location.setText(AnimalBooks.get(position).getLocation());
            Glide.with(AnimalBookActivity.this).load(AnimalBooks.get(position).getImage()).into(((CustomViewHolder) holder).animal);

            //위치 인텐트 넘어가는 곳
            ((CustomViewHolder)holder).location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            final String animalID = AnimalBooks.get(position).getAnimalID();

            databaseReference.child("Users").child(user.getUserEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    ((CustomViewHolder)holder).likes.setText(String.valueOf(AnimalBooks.get(position).getLiker().size()));

                    if(AnimalBooks.get(position).getLiker().contains(user.getUserEmail())){
                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.filled);
                    }else {
                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.empty);
                    }

                    ((CustomViewHolder)holder).like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    animalBook3 = dataSnapshot.getValue(AnimalBook.class);
                                    assert animalBook3 != null;
                                    if (animalBook3.addLiker(user.getUserEmail())) {
                                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.filled);
                                        ((CustomViewHolder) holder).likes.setText(""+animalBook3.getLiker().size());
                                    } else {
                                        ((CustomViewHolder) holder).like.setBackgroundResource(R.drawable.empty);
                                        ((CustomViewHolder) holder).likes.setText("" + animalBook3.getLiker().size());
                                    }

                                    databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).child("liker").setValue(animalBook3.getLiker());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

        @Override
        public int getItemCount() {
            return AnimalBooks.size();
        }
    }

}