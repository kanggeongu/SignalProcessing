package com.exam.signalprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListHelper {

    private List<ExpandedMenuModel> listDataHeader;
    private HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private List<String> heading=new ArrayList<>();
    private List<String> university=new ArrayList<>();

    public List<ExpandedMenuModel> getListDataHeader() {
        return listDataHeader;
    }

    public HashMap<ExpandedMenuModel, List<String>> getListDataChild() {
        return listDataChild;
    }

    public ExpandableListHelper(List<String> university){
        listDataHeader=new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();
        this.university=university;
    }

    public void initHeading(){
        heading.add("자유게시판");
        heading.add("신규 동물 요청");
        heading.add("동물 도감");
        heading.add("이름 공모전");
    }

    public void initItem(){
        initHeading();
        for(int i=0;i<university.size();i++) {
            ExpandedMenuModel item = new ExpandedMenuModel();
            item.setIconName(university.get(i));
            listDataHeader.add(item);
            listDataChild.put(listDataHeader.get(i), heading);
        }
    }

}
