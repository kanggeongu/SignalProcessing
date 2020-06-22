package com.exam.signalprocessing;

import java.util.ArrayList;
import java.util.List;

public class Article {

    private String articleID;
    private String univName;
    private String userID;
    private String content;
    private List<String> lovers;
    private List<String> reporters;
    private String image;
    private String endDate;

    Article() {
        userID = "";
        univName = "";
        content = "";
        image = "";
        lovers = new ArrayList<>();
        reporters = new ArrayList<>();
    }

    Article(String articleID, String univName, String userID, String content, String image, String endDate) {
        this.articleID = articleID;
        this.univName = univName;
        this.userID = userID;
        this.content = content;
        this.image = image;
        lovers = new ArrayList<>();
        reporters = new ArrayList<>();
        this.endDate = endDate;
    }

    public String getArticleID() {
        return articleID;
    }

    public void setArticleID(String articleID) {
        this.articleID = articleID;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
