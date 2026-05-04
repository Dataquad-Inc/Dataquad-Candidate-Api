package com.profile.candidate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="interviews_us")
public class InterviewDetailsUS {

    @Id
    private String interviewId;
    
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
    private Boolean isDeleted;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    private String clientId;
    private String clientName;
    private String consultantEmailId;
    private String consultantId;
    private String consultantName;
    private Integer duration;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime interviewDateTime;
    
    @Lob
    @Column(name = "interview_history", columnDefinition = "TEXT")
    private String interviewHistory;
    
    private String interviewLevel;
    
    @Lob
    @Column(name = "interview_status", columnDefinition = "TEXT")
    private String interviewStatus;
    
    private String interviewerEmailId;
    private boolean isPlaced;
    private String rtrId;
    private String salesExecutive;
    private String salesExecutiveId;
    private String technology;
    private String zoomLink;
    private String remarks;

    public InterviewDetailsUS() {
    }

    // Getters and Setters
    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getConsultantEmailId() {
        return consultantEmailId;
    }

    public void setConsultantEmailId(String consultantEmailId) {
        this.consultantEmailId = consultantEmailId;
    }

    public String getConsultantId() {
        return consultantId;
    }

    public void setConsultantId(String consultantId) {
        this.consultantId = consultantId;
    }

    public String getConsultantName() {
        return consultantName;
    }

    public void setConsultantName(String consultantName) {
        this.consultantName = consultantName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(LocalDateTime interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }

    public String getInterviewHistory() {
        return interviewHistory;
    }

    public void setInterviewHistory(String interviewHistory) {
        this.interviewHistory = interviewHistory;
    }

    public String getInterviewLevel() {
        return interviewLevel;
    }

    public void setInterviewLevel(String interviewLevel) {
        this.interviewLevel = interviewLevel;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }

    public String getInterviewerEmailId() {
        return interviewerEmailId;
    }

    public void setInterviewerEmailId(String interviewerEmailId) {
        this.interviewerEmailId = interviewerEmailId;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void setIsPlaced(boolean isPlaced) {
        this.isPlaced = isPlaced;
    }

    public String getRtrId() {
        return rtrId;
    }

    public void setRtrId(String rtrId) {
        this.rtrId = rtrId;
    }

    public String getSalesExecutive() {
        return salesExecutive;
    }

    public void setSalesExecutive(String salesExecutive) {
        this.salesExecutive = salesExecutive;
    }

    public String getSalesExecutiveId() {
        return salesExecutiveId;
    }

    public void setSalesExecutiveId(String salesExecutiveId) {
        this.salesExecutiveId = salesExecutiveId;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
