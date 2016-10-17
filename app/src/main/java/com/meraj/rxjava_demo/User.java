package com.meraj.rxjava_demo;

/**
 * Created by meraj on 18/09/16.
 */
public class User {
    private String name;
    private int status;

    public User(String name, int status) {
        this.name = name;
        this.status = status;
    }

    public int getSecurityStatus() {
        return this.status;
    }

    public String getUserName() {
        return this.name;
    }
}