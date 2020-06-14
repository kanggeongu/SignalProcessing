package com.example.signalprocessing;

import java.util.List;

public class NameContestViewData implements Comparable<NameContestViewData> {

    private String ID;
    private String Image;
    private List<String> participants;
    private String oneSentence;
    private String Time;
    private String userName;

    public NameContestViewData(String ID, String image, List<String> participants, String oneSentence, String Time, String userName) {
        this.ID = ID;
        Image = image;
        this.participants = participants;
        this.oneSentence = oneSentence;
        this.Time = Time;
        this.userName = userName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getOneSentence() {
        return oneSentence;
    }

    public void setOneSentence(String oneSentence) {
        this.oneSentence = oneSentence;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public int compareTo(NameContestViewData o) {
        Long myStartTime = Long.parseLong(this.Time.substring(0,14));
        Long yourStartTime = Long.parseLong(o.Time.substring(0,14));
        if(myStartTime < yourStartTime) return 1;
        else if(myStartTime > yourStartTime) return -1;
        return 0;
    }
}
