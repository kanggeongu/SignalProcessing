package com.example.signalprocessing;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private int mgroupId;
    private int mitemId;
    private String mUniv;
    private Context mContext;
    private List<ExpandedMenuModel> mListDataHeader; // 데이터의 헤더
    private HashMap<ExpandedMenuModel,List<String>> mListDataChild; // 자식
    private ExpandableListView expandList;

    public ExpandableListAdapter(int mgroupId,int mitemId,String mUniv,Context mContext, List<ExpandedMenuModel> mListDataHeader, HashMap<ExpandedMenuModel, List<String>> mListDataChild, ExpandableListView expandList) {
        this.mgroupId=mgroupId;
        this.mitemId=mitemId;
        this.mUniv=mUniv;
        this.mContext = mContext;
        this.mListDataHeader = mListDataHeader;
        this.mListDataChild = mListDataChild;
        this.expandList = expandList;
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int childCount=0;
        childCount=this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).size();
        return childCount;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ExpandedMenuModel headerTitle=(ExpandedMenuModel)getGroup(groupPosition);
        if(convertView==null){
            LayoutInflater inflater=(LayoutInflater)this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.listheader,null);
        }
        TextView listheader=(TextView)convertView.findViewById(R.id.submenu);
        listheader.setText(headerTitle.getIconName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText=(String)getChild(groupPosition,childPosition);
        if(convertView==null){
            LayoutInflater inflater=(LayoutInflater)this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.list_submenu,null);
        }
        TextView textlistChild=(TextView)convertView.findViewById(R.id.submenu);
        textlistChild.setText(childText);
        if(groupPosition==mgroupId&&childPosition==mitemId){
            textlistChild.setTextColor(Color.parseColor("#FFC000"));
            textlistChild.setTextSize(20);
        }
        else{
            textlistChild.setTextColor(Color.parseColor("#7F7F7F"));
            textlistChild.setTextSize(15);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
