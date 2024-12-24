package com.profile.candidate.dto;

import com.profile.candidate.model.CandidateDetails;

import java.time.LocalDateTime;

public class InterviewDto {

    private LocalDateTime interviewDateTime;
    private Integer duration;
    private String zoomLink;
    private LocalDateTime scheduledTimeStamp;
    private String userId;
    private String jobId;
    private String clientName;
    private CandidateDetails candidateDetails;  // Use CandidateDetails object instead of individual fields
    private String userEmail;
    private String interviewLevel;
    private String clientEmail;

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    // Constructor
    public InterviewDto() {
        // Empty constructor
    }

    // Getters and Setters
    public CandidateDetails getCandidateDetails() {
        return candidateDetails;
    }

    public void setCandidateDetails(CandidateDetails candidateDetails) {
        this.candidateDetails = candidateDetails;
    }

    public String getCandidateId() {
        return candidateDetails != null ? candidateDetails.getCandidateId() : null;
    }

    public String getCandidateFullName() {
        return candidateDetails != null ? candidateDetails.getFullName() : null;
    }

    public String getCandidateContactNo() {
        return candidateDetails != null ? candidateDetails.getContactNumber() : null;
    }

    public String getCandidateEmail() {
        return candidateDetails != null ? candidateDetails.getEmailId() : null;
    }

    public LocalDateTime getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(LocalDateTime interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getScheduledTimeStamp() {
        return scheduledTimeStamp;
    }

    public void setScheduledTimeStamp(LocalDateTime scheduledTimeStamp) {
        this.scheduledTimeStamp = scheduledTimeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getInterviewLevel() {
        return interviewLevel;
    }

    public void setInterviewLevel(String interviewLevel) {
        this.interviewLevel = interviewLevel;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    @Override
    public String toString() {
        return "InterviewDto{" +
                "interviewDateTime=" + interviewDateTime +
                ", duration=" + duration +
                ", zoomLink='" + zoomLink + '\'' +
                ", scheduledTimeStamp=" + scheduledTimeStamp +
                ", userId='" + userId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", clientName='" + clientName + '\'' +
                ", candidateDetails=" + candidateDetails +
                ", userEmail='" + userEmail + '\'' +
                ", interviewLevel='" + interviewLevel + '\'' +
                '}';
    }

    public String getClientEmail() {
        return clientEmail;
    }
}
