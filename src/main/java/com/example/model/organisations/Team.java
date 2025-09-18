package com.example.model.organisations;

import java.time.LocalDate;
import java.util.List;

public class Team {
    private String id;
    private String xid;
    private String name;
    private String email;
    private List<String> brokerCodes;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer timeLimitation;
    private String status;
    private Boolean endorsementsToBlockCorrections;
    private Boolean digitalDocumentAccess;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getBrokerCodes() {
        return brokerCodes;
    }

    public void setBrokerCodes(List<String> brokerCodes) {
        this.brokerCodes = brokerCodes;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Integer getTimeLimitation() {
        return timeLimitation;
    }

    public void setTimeLimitation(Integer timeLimitation) {
        this.timeLimitation = timeLimitation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getEndorsementsToBlockCorrections() {
        return endorsementsToBlockCorrections;
    }

    public void setEndorsementsToBlockCorrections(Boolean endorsementsToBlockCorrections) {
        this.endorsementsToBlockCorrections = endorsementsToBlockCorrections;
    }

    public Boolean getDigitalDocumentAccess() {
        return digitalDocumentAccess;
    }

    public void setDigitalDocumentAccess(Boolean digitalDocumentAccess) {
        this.digitalDocumentAccess = digitalDocumentAccess;
    }
} 