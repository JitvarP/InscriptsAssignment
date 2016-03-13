package com.example.jitvar.inscriptsassignment.WebEntities;

/**
 * Created by jitvar on 13/3/16.
 */
public class WeMessage {

    private String message;
    private String role;
    private long timestamp;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
