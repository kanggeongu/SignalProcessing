package com.exam.signalprocessing;

import java.util.ArrayList;
import java.util.List;

public class NameContestIdea implements Comparable<NameContestIdea>{

    private String ID;
    private String userName;
    private String animalName;
    private String reason;
    private List<String> voters;
    private List<String> lovers;
    private List<String> reporters;

    NameContestIdea() {
        voters = new ArrayList<>();
        lovers = new ArrayList<>();
        reporters = new ArrayList<>();
    }

    NameContestIdea(String ID, String userName, String animalName, String reason) {
        this.ID = ID;
        this.userName = userName;
        this.animalName = animalName;
        this.reason = reason;
        voters = new ArrayList<>();
        lovers = new ArrayList<>();
        reporters = new ArrayList<>();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAnimalName() {
        return animalName;
    }

    public void setAnimalName(String animalName) {
        this.animalName = animalName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getVoters() {
        return voters;
    }

    public void setVoters(List<String> voters) {
        this.voters = voters;
    }

    public List<String> getLovers() {
        return lovers;
    }

    public void setLovers(List<String> lovers) {
        this.lovers = lovers;
    }

    public List<String> getReporters() {
        return reporters;
    }

    public void setReporters(List<String> reporters) {
        this.reporters = reporters;
    }


    public boolean addVoter(String voterID) {
        if (voters.remove(voterID) == false) {
            voters.add(voterID);
            return true;
        }
        return false;
    }

    public boolean addReporter(String reporterID) {
        if (reporters.contains(reporterID) == false) {
            reporters.add(reporterID);
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(NameContestIdea o) {
        int myVoters = this.voters.size();
        int yourVoters = o.voters.size();

        if(myVoters < yourVoters) return 1;
        else if(myVoters > yourVoters) return -1;
        return 0;
    }
}
