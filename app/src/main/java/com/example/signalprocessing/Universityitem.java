package com.example.signalprocessing;

import android.net.Uri;

public class Universityitem implements Comparable<Universityitem>{
    private String universityName;
    private int followers;
    private String photo;

    public Universityitem(){
        universityName="";
        followers=0;
        photo="";
    }

    Universityitem(String Name, String photo){
        this.universityName = Name;
        this.photo = photo;
    }


    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public Uri getPhoto() {
        return Uri.parse(photo);
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }


    @Override
    public int compareTo(Universityitem o) {
        User user = ((UniversityActivity)UniversityActivity.mContext).user;

        String name1 = this.getUniversityName();
        String name2 = o.getUniversityName();

        if(name1.equals(user.getUserUniv())) return -1;
        else if(name2.equals(user.getUserUniv())) return 1;
        else {
            int followers1 = this.getFollowers();
            int followers2 = o.getFollowers();

            if (followers1 == followers2) return 0;
            else if (followers1 < followers2) return 1;
            else return -1;
        }
    }
}
