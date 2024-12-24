package com.profile.candidate.model;

import jakarta.persistence.*;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "candidates")
public class CandidateDetails {

    @Id
    @Column(unique = true, nullable = false)
    private String candidateId;

    @Column(nullable = false)
    @NotBlank(message = "Job ID is required")
    private String jobId;

    // New field userEmail
    @Column(name = "user_email")
    private String userEmail;

    // New field clientEmail
    @Column(name = "client_email")
    private String clientEmail;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    private String emailId;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits and numeric")
    private String contactNumber;

    private String currentOrganization;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @NotNull(message = "Total experience is required")
    @Min(value = 0, message = "Total experience cannot be negative")
    private Integer totalExperience;

    private String relevantExperience;

    private Double currentCTC;

    private Double expectedCTC;

    private String noticePeriod;

    private String currentLocation;

    private String preferredLocation;

    @ElementCollection
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "candidate_id"))
    @Column(name = "skill")
    private List<String> skills;

    private String communicationSkills;

    private Double requiredTechnologiesRating;

    private String overallFeedback;

    private LocalDateTime interviewDateTime;

    private Integer duration; // in minutes

    private LocalDateTime timestamp;

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

    private String zoomLink;

    private String clientName;

    private String interviewLevel;


    // New field profileReceivedDate
    @Column(nullable = false)
    private LocalDate profileReceivedDate;

    @PrePersist
    public void prePersist() {
        if (this.profileReceivedDate == null) {
            this.profileReceivedDate = LocalDate.now();  // Set the date before saving
        }
        if (this.candidateId == null) {
            generateCandidateId();  // Call the method to generate the candidate ID
        }
    }

    public void generateCandidateId() {
        if (this.candidateId == null || this.candidateId.isEmpty()) {
            Random random = new Random();
            int randomNumber = 1000 + random.nextInt(9000);  // Generates a random number between 100 and 999
            this.candidateId = "CAND" + randomNumber;  // Combine the prefix with the random number
        }
    }

    // Getters and Setters for new fields

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public LocalDate getProfileReceivedDate() {
        return profileReceivedDate;
    }

    public void setProfileReceivedDate(LocalDate profileReceivedDate) {
        this.profileReceivedDate = profileReceivedDate;
    }

    // Getters and Setters for existing fields
    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getCurrentOrganization() {
        return currentOrganization;
    }

    public void setCurrentOrganization(String currentOrganization) {
        this.currentOrganization = currentOrganization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Integer getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(Integer totalExperience) {
        this.totalExperience = totalExperience;
    }

    public String getRelevantExperience() {
        return relevantExperience;
    }

    public void setRelevantExperience(String relevantExperience) {
        this.relevantExperience = relevantExperience;
    }

    public Double getCurrentCTC() {
        return currentCTC;
    }

    public void setCurrentCTC(Double currentCTC) {
        this.currentCTC = currentCTC;
    }

    public Double getExpectedCTC() {
        return expectedCTC;
    }

    public void setExpectedCTC(Double expectedCTC) {
        this.expectedCTC = expectedCTC;
    }

    public String getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(String noticePeriod) {
        this.noticePeriod = noticePeriod;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getCommunicationSkills() {
        return communicationSkills;
    }

    public void setCommunicationSkills(String communicationSkills) {
        this.communicationSkills = communicationSkills;
    }

    public Double getRequiredTechnologiesRating() {
        return requiredTechnologiesRating;
    }

    public void setRequiredTechnologiesRating(Double requiredTechnologiesRating) {
        this.requiredTechnologiesRating = requiredTechnologiesRating;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }
}