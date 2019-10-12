package com.example.onekey;

import com.google.firebase.Timestamp;

public class Password {
    private String id;
    private String url;
    private String username;
    private String password;
    private Timestamp timestamp;

    public Password() {}

    public Password(String url, String username, String password, Timestamp timestamp) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }
}


