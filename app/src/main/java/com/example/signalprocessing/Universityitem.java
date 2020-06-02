package com.example.signalprocessing;

import android.net.Uri;

public class Universityitem{
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


}
