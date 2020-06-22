package com.exam.signalprocessing;

import java.io.Serializable;
import java.util.ArrayList;

public class Content implements Serializable {

    private String content;
    private String userName;
    private String contentID;
    private ArrayList<String> reporter;

    public String getContentID() {
        return contentID;
    }

    public void setContentID(String contentID) {
        this.contentID = contentID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userEmail) {
        this.userName = userEmail;
    }

    public Content(){
        content = "";
        userName = "";
        reporter = new ArrayList<>();
    }

    public Content(String content, String userID){
        this.content = content;
        this.userName = userID;
        reporter = new ArrayList<>();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<String> getReporter() {
        return reporter;
    }

    public void setReporter(ArrayList<String> reporter) {
        this.reporter = reporter;
    }

    public boolean addReporter(String reporterID){
        if(!reporter.contains(reporterID)){
            reporter.add(reporterID);
            return true;
        }
        return false;
    }
}
