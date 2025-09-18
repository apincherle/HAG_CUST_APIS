package com.example.model.organisations;

import java.time.LocalDate;

public class Metadata {
    private LocalDate creationDate;
    private String creationChannel;
    private String creationUser;
    private LocalDate modifiedDate;
    private String modifiedChannel;
    private String modifiedUser;

    // Getters and Setters
    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationChannel() {
        return creationChannel;
    }

    public void setCreationChannel(String creationChannel) {
        this.creationChannel = creationChannel;
    }

    public String getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedChannel() {
        return modifiedChannel;
    }

    public void setModifiedChannel(String modifiedChannel) {
        this.modifiedChannel = modifiedChannel;
    }

    public String getModifiedUser() {
        return modifiedUser;
    }

    public void setModifiedUser(String modifiedUser) {
        this.modifiedUser = modifiedUser;
    }
} 