package com.example.mqtt_jms;

class UserMessage {
    String USER_NAME;
    String message;
    String createdAt;

    public UserMessage(String USER_NAME, String message, String createdAt) {
        this.USER_NAME = USER_NAME;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getUSER_NAME() {
        return USER_NAME;
    }

    public void setUSER_NAME(String USER_NAME) {
        this.USER_NAME = USER_NAME;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

