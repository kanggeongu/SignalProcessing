package com.example.signalprocessing;

import java.io.Serializable;

public class User implements Serializable {
    private String userName;
    private String userEmail;
    private String userUniv;

    User() {
        userName = "";
        userEmail = "";
        userUniv="";
    }

    User(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
        userUniv="";
    }

    public String getUserUniv() {
        return userUniv;
    }

    public void setUserUniv(String userUniv) {
        this.userUniv = userUniv;
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
