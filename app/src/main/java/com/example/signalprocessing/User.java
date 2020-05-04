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


}
