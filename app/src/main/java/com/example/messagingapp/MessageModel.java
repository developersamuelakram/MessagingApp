package com.example.messagingapp;

public class MessageModel {


    String username;
    String message;
    String photoid;

    public MessageModel() {
    }

    public MessageModel(String username, String message, String photoid) {
        this.username = username;
        this.message = message;
        this.photoid = photoid;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoid() {
        return photoid;
    }

    public void setPhotoid(String photoid) {
        this.photoid = photoid;
    }
}
