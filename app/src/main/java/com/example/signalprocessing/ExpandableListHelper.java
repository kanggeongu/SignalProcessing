package com.example.signalprocessing;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListHelper {
    //파이어베이스
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private List<ExpandedMenuModel> listDataHeader;
    private HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private List<String> heading=new ArrayList<>();
    private List<String> university=new ArrayList<>();

    private Context context;

    public List<ExpandedMenuModel> getListDataHeader() {
        return listDataHeader;
    }

    public HashMap<ExpandedMenuModel, List<String>> getListDataChild() {
        return listDataChild;
    }

    public ExpandableListHelper(Context context){
        listDataHeader=new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();
        this.context=context;
    }

    public void initUniversity(Context context){
        final ProgressDialog pdialog=new ProgressDialog(context);
        pdialog.setTitle("정보를 불러오는 중입니다");
        pdialog.show();
        databaseReference.child("Universities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                university.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    String name=snapshot.getKey();
                    university.add(name);
                }
                initHeading();
                Log.e("error1",""+university.size());
                Log.e("error2",""+university.toString());
                for(int i=0;i<university.size();i++){
                    ExpandedMenuModel item=new ExpandedMenuModel();
                    item.setIconName(university.get(i));
                    Log.e("error3",university.get(i));
                    listDataHeader.add(item);
                    listDataChild.put(listDataHeader.get(i),heading);
                }
                pdialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void initHeading(){
        heading.add("자유게시판");
        heading.add("신규 동물 요청");
        heading.add("동물 도감");
        heading.add("이름 공모전");
    }

    public void initItem(){
        initUniversity(context);
    }

}
