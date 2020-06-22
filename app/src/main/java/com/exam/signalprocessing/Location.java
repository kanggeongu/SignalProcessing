package com.exam.signalprocessing;

import java.io.Serializable;

public class Location implements Serializable {
    private float x;
    private float y;
    private String userName;
    private String content;

    Location(){
        x=0;
        y=0;
        userName="";
        content="";
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}