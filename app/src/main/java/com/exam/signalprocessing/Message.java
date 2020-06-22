package com.exam.signalprocessing;

import java.io.Serializable;

public class Message implements Serializable {
    private String sender;
    private String receiver;
    private String date;
    private String contents;
    private String isRead;
    private boolean isReported;
    private Long contentID;

    public Message(){

    }

    public Message(String sender, String receiver, String date, String contents) {
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
        this.contents = contents;
        this.isRead="읽지않음";
        this.isReported=false;
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public Long getContentID() {
        return contentID;
    }

    public void setContentID(Long contentID) {
        this.contentID = contentID;
    }
}
