package com.exam.signalprocessing;

public class Restricted {
    private String userEmail;
    private int endDate;

    public Restricted(){

    }

    public Restricted(String userEmail, int endDate) {
        this.userEmail = userEmail;
        this.endDate = endDate;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int getEndDate() {
        return endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }
}
