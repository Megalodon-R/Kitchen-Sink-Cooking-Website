package com.swivel.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

// this is just a constuctor for a user
// a User: anyone who is digitally creating, clicking, or interacting with our website
// mostly for those who wish to have an identity. With that identity they get a name, some security features,
// and personal attachments like the ability to comment or save recipes.

public class User {
    private final long userId;
    private final String username;
    private final String passwordHash;
    private int secPin;
    private String bio;
    private final int clearance;
    private LocalDateTime createdAt;

    
    public User(long userId, String username, String passwordHash, int secPin, String bio, int clearance, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.secPin = secPin;
        this.bio = bio;
        this.clearance = clearance;
        this.createdAt = createdAt;
    }
    
    public long getUserId() { return userId; }

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }
    
    public int getSecPin() { return secPin; }

    public String getBio() { return bio; }

    public int getClearance() { return clearance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
