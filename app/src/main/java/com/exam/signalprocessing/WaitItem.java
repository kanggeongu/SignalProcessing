package com.exam.signalprocessing;

import java.io.Serializable;

public class WaitItem implements Serializable {
    private String writer,name,mean,location,feature,gender;
    private String picture,status;

    WaitItem(){

    }

    public WaitItem(String writer,String name, String mean, String location, String feature,
                    String gender, String picture,String status) {
        this.writer=writer;
        this.name = name;
        this.mean = mean;
        this.location = location;
        this.feature = feature;
        this.gender = gender;
        this.picture = picture;
        this.status=status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
