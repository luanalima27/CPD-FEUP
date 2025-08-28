package com.chat.server.model;

public class User {
    private final String username;
    private final String passwordHash;
    
    public User(String username, String password) {
        this.username = username;
        this.passwordHash = password;
    }
    
    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    
    public boolean validatePassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }
    
    private String hashPassword(String password) { 
        return Integer.toString(password.hashCode());
    }
}