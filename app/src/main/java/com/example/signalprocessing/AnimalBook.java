package com.example.signalprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnimalBook {
    private String name;
    private String mean;
    private String location;
    private String gender;
    private String image;
    private String animalID;
    private ArrayList<String> liker;

    public String getAnimalID() {
        return animalID;
    }

    public void setAnimalID(String animalID) {
        this.animalID = animalID;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<String> getLiker() {
        return liker;
    }

    public void setLiker(ArrayList<String> liker) {
        this.liker = liker;
    }

    public AnimalBook() {
        name = "";
        mean = "";
        location = "";
        gender = "";
        image = "";
        liker = new ArrayList<>();
    }

    public AnimalBook(String name, String mean, String location, String gender, String image) {
        this.name = name;
        this.mean = mean;
        this.location = location;
        this.gender = gender;
        this.image = image;
        liker = new ArrayList<>();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("mean", mean);
        result.put("location", location);
        result.put("gender", gender);
        result.put("image", image);
        result.put("like", liker);

        return result;
    }

    public boolean addLiker(String likerID){
        if (!liker.remove(likerID)) {
            liker.add(likerID);
            return true;
        }
        else {
            return false;
        }
    }
}

