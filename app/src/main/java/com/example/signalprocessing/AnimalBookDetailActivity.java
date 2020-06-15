package com.example.signalprocessing;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AnimalBookDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editName;
    private EditText editMean;
    private EditText editGender;
    private EditText editLocation;
    private EditText editContent;
    private ImageView image;
    private Button add;
    private User user;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private ArrayList<Content> contents;
    private LinearLayoutManager layoutManager;
    private ListAdapter listAdapter;
    private String animalID;
    private String contentID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_book_detail);

        contents = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview);
        editName = findViewById(R.id.editName);
        editGender = findViewById(R.id.editGender);
        editMean = findViewById(R.id.editMean);
        editLocation = findViewById(R.id.editLocation);
        editContent = findViewById(R.id.editContent);
        image = findViewById(R.id.image);
        add = findViewById(R.id.add);
        user = (User)getIntent().getSerializableExtra("userInfo");

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        listAdapter = new ListAdapter();
        recyclerView.setAdapter(listAdapter);

        Intent detail = getIntent();
        editName.setText(detail.getStringExtra("이름"));
        editGender.setText(detail.getStringExtra("성별"));
        editMean.setText(detail.getStringExtra("뜻"));
        editLocation.setText(detail.getStringExtra("위치"));
        animalID = detail.getStringExtra("animalID");
        databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).child("Contents").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contents.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    Content content = snapshot.getValue(Content.class);
                    contents.add(content);
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(getIntent().getByteArrayExtra("사진")!=null) {
            byte[] byteArray = getIntent().getByteArrayExtra("사진");
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            image.setImageBitmap(bitmap);
        }


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimalBookCustomDialog customDialog = new AnimalBookCustomDialog(AnimalBookDetailActivity.this);

                customDialog.callFunction(editContent, animalID, user.getUserEmail());
            }
        });
    }

    class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.animalbook_detail_item_list, parent, false);
            return new CustomViewHolder(view);
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder{

            TextView item;

            CustomViewHolder(@NonNull View view) {
                super(view);

                item = view.findViewById(R.id.item);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        contentID = contents.get(getAdapterPosition()).getContentID();
                        registerForContextMenu(item);
                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            ((ListAdapter.CustomViewHolder)holder).item.setText(contents.get(position).getContent());

        }

        @Override
        public int getItemCount() {
            return contents.size();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.report_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.userID:
                databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        AnimalBook animalBook = dataSnapshot.getValue(AnimalBook.class);
                        assert animalBook != null;
                        databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).child("Contents").child(contentID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Content content = dataSnapshot.getValue(Content.class);
                                AlertDialog.Builder builder = new AlertDialog.Builder(AnimalBookDetailActivity.this);

                                builder.setTitle("작성자 정보").setMessage("아이디: " + content.getUserEmail());

                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });


                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.report:
                databaseReference.child("Users").child(user.getUserEmail()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(User.class);
                        databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).child("Contents").child(contentID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Content content = dataSnapshot.getValue(Content.class);
                                assert content != null;
                                if (content.addReporter(user.getUserEmail())) {
                                    Toast.makeText(AnimalBookDetailActivity.this, "신고되었습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AnimalBookDetailActivity.this, "이미 신고되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                                databaseReference.child("AnimalBooks").child("경북대학교").child(animalID).child("Contents").child(content.getContentID()).child("reporter").setValue(content.getReporter());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
        }
        return super.onContextItemSelected(item);
    }
}