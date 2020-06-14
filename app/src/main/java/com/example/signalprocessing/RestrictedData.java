package com.example.signalprocessing;

public class RestrictedData {

    private String userEmail;
    private String content;

    public RestrictedData() {}

    public RestrictedData(String userEmail, String content) {
        this.userEmail = userEmail;
        this.content = content;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
