package com.example.signalprocessing;

import java.util.ArrayList;
import java.util.List;

public class NameContestData {

    private String ID;
    private String image;
    private String location;
    private String gender;
    private String characteristic;
    private String startTime;
    private String endTime;
    private String oneSentence;
    private List<String> voters; // 총 투표자
    private String animalName; //확정된 이름
    private String userName; // 올린 사람
    private String status;

    public NameContestData() {
        this.voters = new ArrayList<>();
    }

    public NameContestData(String ID, String image, String location, String gender, String characteristic, String startTime,
                           String endTime, String oneSentence, String userName, String status) {
        this.ID = ID;
        this.image = image;
        this.location = location;
        this.gender = gender;
        this.characteristic = characteristic;
        this.startTime = startTime;
        this.endTime = endTime;
        this.oneSentence = oneSentence;
        this.voters = new ArrayList<>();
        this.animalName = "";
        this.userName = userName;
        this.status = status;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getOneSentence() {
        return oneSentence;
    }

    public void setOneSentence(String oneSentence) {
        this.oneSentence = oneSentence;
    }

    public List<String> getVoters() {
        return voters;
    }

    public void setVoters(List<String> participants) {
        this.voters = participants;
    }

    public String getAnimalName() {
        return animalName;
    }

    public void setAnimalName(String animalName) {
        this.animalName = animalName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean addVoter(String voterID) {
        if (voters.remove(voterID) == false) {
            voters.add(voterID);
            return true;
        }
        return false;
    }
}
