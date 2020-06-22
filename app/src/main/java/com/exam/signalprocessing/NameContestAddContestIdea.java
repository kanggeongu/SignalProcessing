package com.exam.signalprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NameContestAddContestIdea extends AppCompatActivity {

    private String NameContestID;
    private EditText editTextName, editTextReason;
    private User user;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_contest_add_contest_idea);

        NameContestID = (String)getIntent().getSerializableExtra("contestID");
        user = (User)getIntent().getSerializableExtra("userInformation");
        initPallete();
    }

    private void initPallete() {
        editTextName = (EditText)findViewById(R.id.editTextName);
        editTextReason = (EditText)findViewById(R.id.editTextReason);
    }

    private void makeToast(String Text) {
        Toast.makeText(this, Text, Toast.LENGTH_SHORT).show();
    }

    public void onClickAddContestIdea1(View v) {
        String Name = editTextName.getText().toString();
        String Reason = editTextReason.getText().toString();

        if (Name.equals("")) {
            makeToast("이름을 적어주세요!");
            return;
        }
        if (Reason.equals("")) {
            makeToast("이유를 적어주세요!");
            return;
        }

        Long now = System.currentTimeMillis();
        String ID = Long.toString(now);

        NameContestIdea nameContestIdea = new NameContestIdea(ID, user.getUserName(), Name, Reason);
        databaseReference.child("NameContests").child(user.getUserUniv()).child(NameContestID).child("Ideas").child(ID).setValue(nameContestIdea);
        makeToast("업로드 완료되었습니다.");
        finish();
    }
}
