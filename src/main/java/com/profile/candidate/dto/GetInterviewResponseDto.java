package com.profile.candidate.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class GetInterviewResponseDto {

    private String jobId;                   // Job ID
    private String candidateId;             // Candidate ID
    private String candidateFullName;       // Candidate Full Name
    private String candidateContactNo;      // Candidate Contact Number
    private String candidateEmailId;                 // Candidate Email ID
    private String userEmail;               // User's Email
    private String userId;                  // User ID (interviewer)
    private OffsetDateTime interviewDateTime; // Interview Date & Time
    private Integer duration;               // Duration of the interview
    private String zoomLink;                // Zoom Link for the interview
    private LocalDateTime interviewScheduledTimestamp; // Timestamp when interview is scheduled
    private String clientEmail;             // Client Email
    private String clientName;              // Client Name
    private String interviewLevel;          // Interview Level (e.g., 1st, 2nd round)
    private String interviewStatus;  // New field for status


    // Constructor
    public GetInterviewResponseDto(String jobId, String candidateId, String candidateFullName,
                                   String candidateContactNo, String emailId, String userEmail,
                                   String userId, OffsetDateTime interviewDateTime, Integer duration,
                                   String zoomLink, LocalDateTime interviewScheduledTimestamp,
                                   String clientEmail, String clientName, String interviewLevel,String interviewStatus) {
        this.jobId = jobId;
        this.candidateId = candidateId;
        this.candidateFullName = candidateFullName;
        this.candidateContactNo = candidateContactNo;
        this.candidateEmailId = emailId;
        this.userEmail = userEmail;
        this.userId = userId;
        this.interviewDateTime = interviewDateTime;
        this.duration = duration;
        this.zoomLink = zoomLink;
        this.interviewScheduledTimestamp = interviewScheduledTimestamp;
        this.clientEmail = clientEmail;
        this.clientName = clientName;
        this.interviewLevel = interviewLevel;
        this.interviewStatus = interviewStatus;
    }

    // Getters and Setters
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateFullName() {
        return candidateFullName;
    }

    public void setCandidateFullName(String candidateFullName) {
        this.candidateFullName = candidateFullName;
    }

    public String getCandidateContactNo() {
        return candidateContactNo;
    }

    public void setCandidateContactNo(String candidateContactNo) {
        this.candidateContactNo = candidateContactNo;
    }

    public String getCandidateEmailId() {
        return candidateEmailId;
    }

    public void setCandidateEmailId(String candidateEmailId) {
        this.candidateEmailId = candidateEmailId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public OffsetDateTime getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(OffsetDateTime interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    public LocalDateTime getInterviewScheduledTimestamp() {
        return interviewScheduledTimestamp;
    }

    public void setInterviewScheduledTimestamp(LocalDateTime interviewScheduledTimestamp) {
        this.interviewScheduledTimestamp = interviewScheduledTimestamp;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getInterviewLevel() {
        return interviewLevel;
    }

    public void setInterviewLevel(String interviewLevel) {
        this.interviewLevel = interviewLevel;
    }

    @Override
    public String toString() {
        return "GetInterviewResponseDto{" +
                "jobId='" + jobId + '\'' +
                ", candidateId='" + candidateId + '\'' +
                ", candidateFullName='" + candidateFullName + '\'' +
                ", candidateContactNo='" + candidateContactNo + '\'' +
                ", emailId='" + candidateEmailId + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userId='" + userId + '\'' +
                ", interviewDateTime=" + interviewDateTime +
                ", duration=" + duration +
                ", zoomLink='" + zoomLink + '\'' +
                ", interviewScheduledTimestamp=" + interviewScheduledTimestamp +
                ", clientEmail='" + clientEmail + '\'' +
                ", clientName='" + clientName + '\'' +
                ", interviewLevel='" + interviewLevel + '\'' +
                '}';
    }
}
