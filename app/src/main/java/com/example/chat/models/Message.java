package com.example.chat.models;

import java.util.Date;

public class Message {
    private String senderId, receiverId, messageContent, dateTime;
    private Date dateObject;

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date date) {
        this.dateObject = date;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessage(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
