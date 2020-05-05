package com.example.signalprocessing;

import java.io.Serializable;

public class User implements Serializable {
    private String userName;
    private String userEmail;

    User() {
        userName = "";
        userEmail = "";
    }

    User(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
