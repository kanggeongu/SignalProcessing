package com.exam.signalprocessing;

import java.util.ArrayList;
import java.util.List;

public class Cocomment {

    private String cocommentID;
    private String univName;
    private String userID;
    private String content;
    private List<String> lovers;
    private List<String> reporters;
    private String endDate;

    Cocomment() {
        cocommentID = "";
        userID = "";
        content = "";
        lovers = new ArrayList<>();
        reporters = new ArrayList<>();
    }

    Cocomment(String cocommentID, String univName, String userID, String content, String endDate) {
        this.cocommentID = cocommentID;
        this.univName = univName;
        this.userID = userID;
        this.content = content;
        lovers = new ArrayList<>();
        reporters = new ArrayList<>();
        this.endDate = endDate;
    }

    public String getCocommentID() {
        return cocommentID;
    }

    public void setCocommentID(String cocommentID) {
        this.cocommentID = cocommentID;
    }

    public String getUnivName() {
        return univName;
    }

    public void setUnivName(String univName) {
        this.univName = univName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean addLover(String loverID) {
        if (lovers.remove(loverID) == false) {
            lovers.add(loverID);
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
}
