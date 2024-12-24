package com.profile.candidate.dto;


public class CandidateResponseDto {

    private String message;
    private String candidateId;
    private String employeeId;
    private String jobId;

    // Constructor
    public CandidateResponseDto(String message, String candidateId, String employeeId, String jobId) {
        this.message = message;
        this.candidateId = candidateId;
        this.employeeId = employeeId;
        this.jobId = jobId;

    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }
}

