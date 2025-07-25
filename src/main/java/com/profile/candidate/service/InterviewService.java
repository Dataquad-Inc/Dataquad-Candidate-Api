package com.profile.candidate.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.profile.candidate.dto.*;
import com.profile.candidate.exceptions.*;
import com.profile.candidate.model.CandidateDetails;
import com.profile.candidate.model.InterviewDetails;
import com.profile.candidate.model.Submissions;
import com.profile.candidate.repository.CandidateRepository;
import com.profile.candidate.repository.InterviewRepository;
import com.profile.candidate.repository.SubmissionRepository;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InterviewService {

    @Autowired
    InterviewEmailService emailService;
    @Autowired
    CandidateRepository candidateRepository;
    @Autowired
    private  InterviewRepository interviewRepository;
    @Autowired
    SubmissionRepository submissionRepository;

    private static final Logger logger = LoggerFactory.getLogger(InterviewService.class);

    public InterviewResponseDto scheduleInterview(String userId, String candidateId, OffsetDateTime interviewDateTime, Integer duration,
                                                  String zoomLink, String userEmail, List<String> clientEmails,
                                                  String clientName, String interviewLevel, String externalInterviewDetails, String jobId, String fullName,
                                                  String contactNumber, String candidateEmailId, boolean skipNotification,String assignedTo,String comments) throws JsonProcessingException {

        System.out.println("Starting to schedule interview for userId: " + userId + " and candidateId: " + candidateId);
        if (candidateId == null)
            throw new CandidateNotFoundException("Candidate ID cannot be null for userId: " + userId);
        Optional<CandidateDetails> candidate = candidateRepository.findById(candidateId);
        if (candidate.isEmpty()) {
            logger.error("No Candidate Found with Id {}", candidateId);
            throw new CandidateNotFoundException("Invalid Candidate Id " + candidateId);
        }
        if (submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId, jobId) == null) {
            throw new JobNotFoundException("Candidate Not Applied for Job " + jobId);
        }
        Optional<Submissions> candidateDetails = submissionRepository.findBySubmissionIdAndUserId(candidateId, userId);

        InterviewDetails inti = interviewRepository.findByCandidateIdAndUserIdAndClientNameAndJobId(candidateId, userId, clientName, jobId);
        InterviewDetails interviewDetails = new InterviewDetails();

        if (candidateDetails.isEmpty())
            new CandidateNotFoundException("Candidate not found for userId: " + userId + " and candidateId: " + candidateId);
        // Ensure no interview is already scheduled
        if (inti != null) {
            throw new InterviewAlreadyScheduledException("An interview is already scheduled for candidate ID: " + candidateId);
        }
        // Update candidate details with provided information
        interviewDetails.setUserEmail(userEmail);
        interviewDetails.setClientEmailList(clientEmails);
        setDefaultEmailsIfMissing(interviewDetails);
        // Determine Interview Type if not provided
        if (interviewLevel == null || interviewLevel.isEmpty()) {
            interviewLevel = determineInterviewType(clientEmails, zoomLink);
        }
        interviewDetails.setInterviewLevel(interviewLevel);

            interviewDetails.setClientEmailList(clientEmails);
            interviewDetails.setZoomLink(zoomLink);
//
        interviewDetails.setCandidateId(candidateId);
        interviewDetails.setUserId(userId);
        interviewDetails.setUserEmail(userEmail);
        interviewDetails.setInterviewDateTime(interviewDateTime);
        interviewDetails.setDuration(duration);
        interviewDetails.setZoomLink(zoomLink);
        interviewDetails.setClientEmailList(clientEmails);
        interviewDetails.setClientName(clientName);
        interviewDetails.setInterviewLevel(interviewLevel);
        interviewDetails.setExternalInterviewDetails(externalInterviewDetails);
        interviewDetails.setFullName(fullName);
        interviewDetails.setContactNumber(contactNumber);
        interviewDetails.setCandidateEmailId(candidateEmailId);
        interviewDetails.setTimestamp(LocalDateTime.now());
        interviewDetails.setIsPlaced(false);
        interviewDetails.setRecruiterName(candidateRepository.findUserNameByEmail(userEmail));

        String clientId = interviewRepository.findClientIdByClientName(clientName);
        if (clientId == null) throw new InvalidClientException("No Client With Name :" + clientName);

        interviewDetails.setClientId(clientId);
        String interviewId = candidateId + "_" + clientId + "_" + jobId;
        interviewDetails.setInterviewId(interviewId);
        interviewDetails.setJobId(jobId);

        Submissions submissions=submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId,jobId);
        submissions.setStatus("MOVED TO INTERVIEW");
           if(interviewLevel.equalsIgnoreCase("INTERNAL")){
                interviewDetails.setComments(comments);
           }
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode statusArray = objectMapper.createArrayNode();
        ObjectNode statusEntry = objectMapper.createObjectNode();
        statusEntry.put("stage", 1);
        statusEntry.put("status", "SCHEDULED");
        statusEntry.put("interviewLevel", interviewLevel);
        statusEntry.put("timestamp", OffsetDateTime.now().toString());
        statusArray.add(statusEntry);
        interviewDetails.setInterviewStatus(objectMapper.writeValueAsString(statusArray));

        if(assignedTo!=null && interviewLevel.equals("INTERNAL")){
            interviewDetails.setAssignedTo(assignedTo);
            interviewDetails.setCoordinatorName(interviewRepository.findUsernameByUserId(assignedTo));
        }
        else if(assignedTo!=null && !interviewLevel.equals("INTERNAL")){
            throw new InterviewNotScheduledException("For INTERNAL Interviews Only we Can Assign to Co-Ordinators");
        }else{
            interviewDetails.setAssignedTo(null);
            interviewDetails.setCoordinatorName(null);
        }
        // Save candidate details to the database
        try {
            interviewRepository.save(interviewDetails);
            logger.info("Interview Scheduled Successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error while saving candidate data.", e);
        }
        // Send email notifications about the interview
        if (interviewDetails.getClientEmail().isEmpty()) {
            System.err.println("Invalid client email: " + interviewDetails.getClientEmail());
        }

        // Sending Emails
        if (!skipNotification) {
                String jobTitle = interviewRepository.findJobTitleByJobId(jobId);
                String subject = "Interview Scheduled for " + interviewDetails.getFullName();
                // Convert the interview time to IST timezone
                ZoneOffset istOffset = ZoneOffset.of("+05:30");
                OffsetDateTime interviewTimeInIST = interviewDateTime.withOffsetSameInstant(istOffset);
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
                String formattedDate = interviewTimeInIST.format(dateFormatter);  // formatted date in IST
                String formattedTime = interviewTimeInIST.format(timeFormatter);  // formatted time in IST
                String userName = interviewRepository.findUsernameByUserId(userId);
            if(!interviewLevel.equalsIgnoreCase("INTERNAL")) {
                emailService.sendEmailToUser(userEmail, subject, buildUserScheduleEmailBody(userName, clientName,
                        formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
                emailService.sendEmailsToClients(interviewDetails.getClientEmailList(), subject, buildClientScheduleEmailBody(clientName,
                        formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
                emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildCandidateScheduleEmailBody(interviewDetails.getFullName(),
                        formattedDate, formattedTime, duration, zoomLink, jobTitle, clientName));
            }else{
                String coordinatorName=interviewRepository.findUsernameByUserId(assignedTo);
                String coordinatorEmail=interviewRepository.findUserEmailByUserId(assignedTo);
                logger.info("CoOrdinator Name {} and email {}",coordinatorName,coordinatorEmail);
                emailService.sendEmailToCoordinator(coordinatorEmail, subject, buildCoordinatorScheduleEmailBody(coordinatorName,
                        formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
                emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildCandidateScheduleEmailBody(interviewDetails.getFullName(),
                        formattedDate, formattedTime, duration, zoomLink, jobTitle, clientName));
                emailService.sendEmailToUser(userEmail, subject, buildUserScheduleEmailBody(userName, clientName,
                        formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
            }
        }
        // Prepare the response with interview details
        InterviewResponseDto.InterviewData data = new InterviewResponseDto.InterviewData(
                interviewDetails.getCandidateId(),
                interviewDetails.getUserEmail(),
                interviewDetails.getCandidateEmailId(),
                interviewDetails.getClientEmailList()
        );
        return new InterviewResponseDto(true,
                skipNotification ? "Interview Scheduled successfully." : "Interview Scheduled successfully and notifications sent.",
                data,
                null);
    }

    /**
     * Determines the interview type based on clientEmail and zoomLink.
     */
    private String determineInterviewType(List<String> clientEmail, String zoomLink) {
        return (clientEmail != null && !clientEmail.isEmpty() && zoomLink != null && !zoomLink.isEmpty())
                ? "Internal"
                : "External";
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;  // Early return if email is null or empty
        }
        try {
            email = email.trim();  // Remove leading/trailing whitespace
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();  // Will throw an exception if invalid
            return true;  // Valid email format
        } catch (AddressException e) {
            return false;  // Invalid email format
        }
    }

    private void setDefaultEmailsIfMissing(InterviewDetails interviewDetails) {
        if (interviewDetails.getUserEmail() == null) {
            interviewDetails.setUserEmail(interviewDetails.getUserEmail());  // Set to default or handle differently
        }
        if (interviewDetails.getClientEmail() == null) {
            interviewDetails.setClientEmail(interviewDetails.getClientEmail());  // Set to default or handle differently
        }
    }

    public boolean isInterviewScheduled(String candidateId, String jobId, OffsetDateTime interviewDateTime) {
        // Query the repository to check if there's already an interview scheduled at that time
        Optional<InterviewDetails> existingInterview = interviewRepository.findByCandidateIdAndJobIdAndInterviewDateTime(candidateId, jobId, interviewDateTime);
        // Return true if an interview already exists, otherwise false
        return existingInterview.isPresent();
    }

    public InterviewResponseDto updateScheduledInterview(
            String userId,
            String candidateId,
            String candidateEmailId,
            String jobId,
            OffsetDateTime interviewDateTime,
            Integer duration,
            String zoomLink,
            String userEmail,
            List<String> clientEmails,
            String clientName,
            String interviewLevel,
            String externalInterviewDetails,
            String internalFeedback,
            String interviewStatus,
            boolean skipNotification,
            String assignedTo) {

        logger.info("Starting interview update for userId: {} and candidateId: {}", userId, candidateId);

        if (candidateId == null) {
            throw new CandidateNotFoundException("Candidate ID cannot be null for userId: " + userId);
        }
        // Retrieve candidate details
        InterviewDetails interview = interviewRepository.findByCandidateIdAndUserIdAndJobId(candidateId, userId, jobId);
        System.out.println(interview);
        if (interview == null)
            throw new CandidateNotFoundException("No Interview found for userId: " + userId + " and candidateId: " + candidateId + " for JobId: " + jobId);

        InterviewDetails interviewDetails = interviewRepository.findByCandidateIdAndUserIdAndClientNameAndJobId(candidateId, userId, clientName, jobId);
        if (interviewDetails == null) {
            throw new InterviewNotScheduledException("No interview scheduled for candidate ID: " + candidateId + " For Client " + clientName + " For Job Id " + jobId);
        }
        if (interviewDateTime != null) interviewDetails.setInterviewDateTime(interviewDateTime);
        if (duration != null) interviewDetails.setDuration(duration);
        if (zoomLink != null && !zoomLink.isEmpty()) interviewDetails.setZoomLink(zoomLink);
        if (userEmail != null && !userEmail.isEmpty()) interviewDetails.setUserEmail(userEmail);
        if (clientEmails != null && !clientEmails.isEmpty()) interviewDetails.setClientEmailList(clientEmails);
        if (clientName != null && !clientName.isEmpty()) interviewDetails.setClientName(clientName);
        if (interviewLevel != null && !interviewLevel.isEmpty()) interviewDetails.setInterviewLevel(interviewLevel);
        if (externalInterviewDetails != null && !externalInterviewDetails.isEmpty())
            interviewDetails.setExternalInterviewDetails(externalInterviewDetails);
        if (internalFeedback != null && !internalFeedback.isEmpty()) {
            interviewDetails.setInternalFeedback(internalFeedback);
        }
          if(interviewLevel.equalsIgnoreCase("INTERNAL") && interviewStatus.equalsIgnoreCase("REJECTED")){

              Submissions submissions=submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId,jobId);
              submissions.setStatus("SCREEN REJECT");
          }
          if(interviewLevel.equalsIgnoreCase("EXTERNAL") ||
                  interviewLevel.equalsIgnoreCase("EXTERNAL-L1") ||
                  interviewLevel.equalsIgnoreCase("EXTERNAL-L2") ||
                  interviewLevel.equalsIgnoreCase("FINAL") && interviewStatus.equalsIgnoreCase("REJECTED")){
              Submissions submissions=submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId,jobId);
              submissions.setStatus("CLIENT REJECT");
          }
        // Handle the interview status update if provided
        if (interviewStatus != null && !interviewStatus.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode historyArray;
            try {
                String existingStatus = interviewDetails.getInterviewStatus();
                if (existingStatus != null && !existingStatus.isEmpty()) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(existingStatus);
                        if (jsonNode.isArray()) {
                            historyArray = (ArrayNode) jsonNode;
                        } else {
                            logger.error("Existing interviewStatus is not a valid JSON array: {}", existingStatus);
                            historyArray = objectMapper.createArrayNode(); // Reset to new array
                        }
                    } catch (JsonProcessingException e) {
                        logger.error("Error parsing existing interviewStatus JSON for candidate {}: {}",
                                interviewDetails.getCandidateId(), e.getMessage());
                        historyArray = objectMapper.createArrayNode(); // Reset on failure
                    }
                } else {
                    historyArray = objectMapper.createArrayNode(); // Start fresh if no status exists
                }
                // If the status is provided, don't add "Scheduled" unless this is the first entry
                int nextStage = historyArray.size() + 1; // Changed 'round' to 'stage'
                ObjectNode newEntry = objectMapper.createObjectNode();
                // Add the current status (from UI)
                newEntry.put("stage", nextStage);
                newEntry.put("status", interviewStatus);// The status passed from the UI
                newEntry.put("interviewLevel", interviewLevel);
                newEntry.put("timestamp", OffsetDateTime.now().toString());

                historyArray.add(newEntry);
                // Debugging Log
                logger.info("Updated Interview Status JSON for Candidate {}: {}",
                        interviewDetails.getCandidateId(), objectMapper.writeValueAsString(historyArray));
                interviewDetails.setInterviewStatus(objectMapper.writeValueAsString(historyArray));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing interview status JSON", e);
            }
        }
        // Determine interview type if interviewLevel is null
        if (interviewDetails.getInterviewLevel() == null) {
            interviewDetails.setInterviewLevel(determineInterviewType(clientEmails, zoomLink));
        }
            interviewDetails.setClientEmailList(clientEmails);
            interviewDetails.setZoomLink(zoomLink);
        // Update timestamp
        interviewDetails.setTimestamp(LocalDateTime.now());
        // Save updated candidate details
        // updating isPlaced field if status is Placed.

        interviewRepository.save(interviewDetails);
        logger.info("Interview details updated successfully for candidateId: {}", candidateId);
        // Prepare email content
        ZoneOffset istOffset = ZoneOffset.of("+05:30");
        OffsetDateTime interviewTimeInIST = (interviewDateTime != null) ? interviewDateTime.withOffsetSameInstant(istOffset) : null;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedDate = (interviewTimeInIST != null) ? interviewTimeInIST.format(dateFormatter) : "N/A";
        String formattedTime = (interviewTimeInIST != null) ? interviewTimeInIST.format(timeFormatter) : "N/A";
        String formattedDuration = (duration != null) ? duration + " minutes" : "N/A";
        String formattedZoomLink = (zoomLink != null && !zoomLink.isEmpty()) ? "<a href='" + zoomLink + "'>Click here to join</a>" : "N/A";
        String subject = "Interview Update for " + interviewDetails.getFullName();

        interviewDetails.setTimestamp(LocalDateTime.now());
        interviewRepository.save(interviewDetails);
        String userName = interviewRepository.findUsernameByUserId(userId);
        String jobTitle = interviewRepository.findJobTitleByJobId(jobId);
        String canceledSubject = "Interview Cancelled for " + interviewDetails.getFullName();
        String canceledEmailBody = String.format(
                "<p>Hello %s,</p><p>We regret to inform you that your interview has been cancelled.</p>"
                        + "<p>If you have any questions, please contact support.</p><p>Best regards,<br>Interview Team</p>",
                interviewDetails.getFullName());
        // Validate userEmail before sending the email
        String userEmailId = interviewDetails.getUserEmail();
        if (userEmailId == null || userEmailId.isEmpty()) {
            logger.error("User email is null or empty for candidateId: {}", candidateId);
        } else {
            if (!skipNotification) {
                try {
                    if (interviewStatus == null || interviewStatus.isEmpty()) {
                        logger.warn("No interview status found, skipping email notifications");
                    } else {
                        if (!interviewLevel.equalsIgnoreCase("INTERNAL")) {
                            switch (interviewStatus.toLowerCase()) {
                                case "scheduled":
                                case "rescheduled":
                                    emailService.sendEmailToUser(userEmailId, subject, buildUpdateUserEmailBody(userName, clientName,
                                            formattedDate, formattedTime, formattedDuration, formattedZoomLink, jobTitle, interviewDetails.getFullName()));// ✅ fixed
                                    emailService.sendEmailsToClients(interviewDetails.getClientEmailList(), subject, buildUpdateClientEmailBody(interviewDetails.getClientName(),
                                            formattedDate, formattedTime, formattedDuration, formattedZoomLink, jobTitle, interviewDetails.getFullName()));
                                    emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildUpdateCandidateEmailBody(interviewDetails.getFullName(),
                                            formattedDate, formattedTime, formattedDuration, formattedZoomLink, jobTitle, interviewDetails.getClientName()));
                                    break;
                                case "cancelled":
                                    emailService.sendEmailToCandidate(candidateEmailId, canceledSubject, canceledEmailBody);  // ✅ fixed
                                    break;
                                case "rejected":
                                case "placed":
                                    logger.info("No emails to be sent for status: {}", interviewStatus);
                                    break;
                                default:
                                    logger.warn("Unknown interview status: {}", interviewStatus);
                            }
                        }else{
                            String coordinatorName=interviewRepository.findUsernameByUserId(assignedTo);
                            String coordinatorEmail=interviewRepository.findUserEmailByUserId(assignedTo);
                            switch (interviewStatus.toLowerCase()) {
                                case "scheduled":
                                case "rescheduled":
                                    emailService.sendEmailToUser(userEmailId, subject, buildUpdateUserEmailBody(userName, clientName,
                                            formattedDate, formattedTime, formattedDuration, formattedZoomLink, jobTitle, interviewDetails.getFullName()));// ✅ fixed
                                    emailService.sendEmailToCoordinator(coordinatorEmail, subject, buildCoordinatorScheduleEmailBody(coordinatorName,
                                            formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
                                    emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildUpdateCandidateEmailBody(interviewDetails.getFullName(),
                                            formattedDate, formattedTime, formattedDuration, formattedZoomLink, jobTitle, interviewDetails.getClientName()));
                                    break;
                                case "cancelled":
                                    emailService.sendEmailToCandidate(candidateEmailId, canceledSubject, canceledEmailBody);  // ✅ fixed
                                    break;
                                case "rejected":
                                case "placed":
                                    logger.info("No emails to be sent for status: {}", interviewStatus);
                                    break;
                                default:
                                    logger.warn("Unknown interview status: {}", interviewStatus);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing interview notifications: {}", e.getMessage(), e);
                }
            }
        }
        // Return updated interview response
        return new InterviewResponseDto(
                true,
                skipNotification ? "Interview updated successfully." : "Interview updated successfully and notifications sent.",
                new InterviewResponseDto.InterviewData(
                        interviewDetails.getCandidateId(),
                        interviewDetails.getUserEmail(),
                        interviewDetails.getCandidateEmailId(),
                        interviewDetails.getClientEmailList()
                ),
                null  // No errors
        );
    }

    public InterviewResponseDto updateScheduledInterviewWithoutUserId(
            String candidateId,
            String candidateEmailId,
            String jobId,
            OffsetDateTime interviewDateTime,
            Integer duration,
            String zoomLink,
            List<String> clientEmails,
            String clientName,
            String interviewLevel,
            String externalInterviewDetails,
            String internalFeedback,
            String interviewStatus,
            boolean skipNotification,
            String comments) {

        logger.info("Starting interview update  and candidateId: {}", candidateId);

        if (candidateId == null) {
            throw new CandidateNotFoundException("Candidate ID cannot be null ");
        }
        // Retrieve candidate details
        InterviewDetails interview = interviewRepository.findByCandidateIdAndJobId(candidateId, jobId);

        if (interview == null)
            throw new CandidateNotFoundException("No Interview found for " + " candidateId: " + candidateId + " for JobId: " + jobId);

        InterviewDetails interviewDetails = interviewRepository.findByCandidateIdAndClientNameAndJobId(candidateId, clientName, jobId);

        if (interviewDetails == null)
            throw new InterviewNotScheduledException("No interview scheduled for candidate ID: " + candidateId + " For Client " + clientName);

        if (interviewDateTime != null) interviewDetails.setInterviewDateTime(interviewDateTime);
        if (duration != null) interviewDetails.setDuration(duration);
        if (zoomLink != null && !zoomLink.isEmpty()) interviewDetails.setZoomLink(zoomLink);
        //if (userEmail != null && !userEmail.isEmpty()) interviewDetails.setUserEmail(userEmail);
        if (clientEmails != null && !clientEmails.isEmpty()) interviewDetails.setClientEmailList(clientEmails);
        if (clientName != null && !clientName.isEmpty()) interviewDetails.setClientName(clientName);
        if (interviewLevel != null && !interviewLevel.isEmpty()) interviewDetails.setInterviewLevel(interviewLevel);
        if (externalInterviewDetails != null && !externalInterviewDetails.isEmpty())
            interviewDetails.setExternalInterviewDetails(externalInterviewDetails);
        if (internalFeedback != null && !internalFeedback.isEmpty()) {
            interviewDetails.setInternalFeedback(internalFeedback);
        }
        if (comments != null && !comments.isEmpty()) {
            interviewDetails.setComments(internalFeedback);
        }
        if(interviewLevel.equalsIgnoreCase("INTERNAL") && interviewStatus.equalsIgnoreCase("REJECTED")){

            Submissions submissions=submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId,jobId);
            submissions.setStatus("SCREEN REJECT");
        }
        if(interviewLevel.equalsIgnoreCase("EXTERNAL") ||
                interviewLevel.equalsIgnoreCase("EXTERNAL-L1") ||
                interviewLevel.equalsIgnoreCase("EXTERNAL-L2") ||
                interviewLevel.equalsIgnoreCase("FINAL") && interviewStatus.equalsIgnoreCase("REJECTED")){
            Submissions submissions=submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId,jobId);
            submissions.setStatus("CLIENT REJECT");
        }
        // Handle the interview status update if provided
        if (interviewStatus != null && !interviewStatus.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode historyArray;
            try {
                String existingStatus = interviewDetails.getInterviewStatus();
                if (existingStatus != null && !existingStatus.isEmpty()) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(existingStatus);
                        if (jsonNode.isArray()) {
                            historyArray = (ArrayNode) jsonNode;
                        } else {
                            logger.error("Existing interviewStatus is not a valid JSON array: {}", existingStatus);
                            historyArray = objectMapper.createArrayNode(); // Reset to new array
                        }
                    } catch (JsonProcessingException e) {
                        logger.error("Error parsing existing interviewStatus JSON for candidate {}: {}",
                                interviewDetails.getCandidateId(), e.getMessage());
                        historyArray = objectMapper.createArrayNode(); // Reset on failure
                    }
                } else {
                    historyArray = objectMapper.createArrayNode(); // Start fresh if no status exists
                }
                // If the status is provided, don't add "Scheduled" unless this is the first entry
                int nextStage = historyArray.size() + 1; // Changed 'round' to 'stage'
                ObjectNode newEntry = objectMapper.createObjectNode();

                // Add the current status (from UI)
                newEntry.put("stage", nextStage);
                newEntry.put("status", interviewStatus);
                newEntry.put("interviewLevel", interviewLevel);
                newEntry.put("timestamp", OffsetDateTime.now().toString());

                historyArray.add(newEntry);
                // Debugging Log
                logger.info("Updated Interview Status JSON for Candidate {}: {}",
                        interviewDetails.getCandidateId(), objectMapper.writeValueAsString(historyArray));
                interviewDetails.setInterviewStatus(objectMapper.writeValueAsString(historyArray));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing interview status JSON", e);
            }
        }
        // Determine interview type if interviewLevel is null
        if (interviewDetails.getInterviewLevel() == null) {
            interviewDetails.setInterviewLevel(determineInterviewType(clientEmails, zoomLink));
        }
            interviewDetails.setClientEmailList(clientEmails);
            interviewDetails.setZoomLink(zoomLink);
        // Update timestamp
        interviewDetails.setTimestamp(LocalDateTime.now());
        // Save updated candidate details
        interviewRepository.save(interviewDetails);
        logger.info("Interview details updated successfully for candidateId: {}", candidateId);
        // Prepare email content
        ZoneOffset istOffset = ZoneOffset.of("+05:30");
        OffsetDateTime interviewTimeInIST = (interviewDateTime != null) ? interviewDateTime.withOffsetSameInstant(istOffset) : null;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedDate = (interviewTimeInIST != null) ? interviewTimeInIST.format(dateFormatter) : "N/A";
        String formattedTime = (interviewTimeInIST != null) ? interviewTimeInIST.format(timeFormatter) : "N/A";
        String formattedDuration = (duration != null) ? duration + " minutes" : "N/A";
        String formattedZoomLink = (zoomLink != null && !zoomLink.isEmpty()) ? "<a href='" + zoomLink + "'>Click here to join</a>" : "N/A";

        String subject = "Interview Update for " + interviewDetails.getFullName();



        String jobTitle = interviewRepository.findJobTitleByJobId(jobId);
        String canceledSubject = "Interview Cancelled for " + interviewDetails.getFullName();
        String canceledEmailBody = String.format(
                "<p>Hello %s,</p><p>We regret to inform you that your interview has been cancelled.</p>"
                        + "<p>If you have any questions, please contact support.</p><p>Best regards,<br>Interview Team</p>",
                interviewDetails.getFullName());
        // Validate userEmail before sending the email
        String userEmailId = interviewDetails.getUserEmail();
        if (userEmailId == null || userEmailId.isEmpty()) {
            logger.error("User email is null or empty for candidateId: {}", candidateId);
        } else {
            if (!skipNotification) {
                try {
                    if (interviewStatus == null || interviewStatus.isEmpty()) {
                        logger.warn("No interview status found, skipping email notifications");
                    } else {
                        switch (interviewStatus.toLowerCase()) {
                            case "scheduled":
                                emailService.sendEmailsToClients(interviewDetails.getClientEmailList(), subject, buildClientScheduleEmailBody(clientName,
                                        formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
                                emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildCandidateScheduleEmailBody(interviewDetails.getFullName(),
                                        formattedDate, formattedTime, duration, zoomLink, jobTitle, clientName));
                                break;
                            case "rescheduled":
                                emailService.sendEmailsToClients(interviewDetails.getClientEmailList(), subject, buildUpdateClientEmailBody(interviewDetails.getClientName(),
                                        formattedDate, formattedTime, formattedDuration, formattedZoomLink, jobTitle, interviewDetails.getFullName()));
                                emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildUpdateClientEmailBody(clientName,
                                        formattedDate, formattedTime, formattedDuration, formattedZoomLink, jobTitle, interviewDetails.getFullName()));
                                break;
                            case "cancelled":
                                emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), canceledSubject, canceledEmailBody);  // ✅ fixed
                                break;
                            case "rejected":
                            case "placed":
                                logger.info("No emails to be sent for status: {}", interviewStatus);
                                break;
                            default:
                                logger.warn("Unknown interview status: {}", interviewStatus);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing interview notifications: {}", e.getMessage(), e);
                }
            }
        }
        return new InterviewResponseDto(
                true,
                skipNotification ? "Interview updated successfully." : "Interview updated successfully and notifications sent.",
                new InterviewResponseDto.InterviewData(
                        interviewDetails.getCandidateId(),
                        interviewDetails.getUserEmail(),
                        interviewDetails.getCandidateEmailId(),
                        interviewDetails.getClientEmailList()
                ),
                null
        );
    }
    public GetInterviewResponse getAllInterviews() {

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        // Use your custom query to fetch scheduled interviews for the current month
        List<InterviewDetails> interviewDetails = interviewRepository
                .findScheduledInterviewsByDateOnly(startOfMonth, endOfMonth);

        List<GetInterviewResponse.InterviewData> dataList = interviewDetails.stream()
                .map(i -> new GetInterviewResponse.InterviewData(
                        i.getInterviewId(),
                        i.getJobId(),
                        i.getCandidateId(),
                        i.getFullName(),
                        i.getContactNumber(),
                        i.getCandidateEmailId(),
                        i.getUserEmail(),
                        i.getUserId(),
                        i.getInterviewDateTime(),
                        i.getDuration(),
                        i.getZoomLink(),
                        i.getTimestamp(),
                        i.getClientEmailList(),
                        i.getClientName(),
                        i.getInterviewLevel(),
                        latestInterviewStatusFromJson(i.getInterviewStatus()),
                        i.getIsPlaced(),
                        i.getRecruiterName(),
                        interviewRepository.findJobTitleByJobId(i.getJobId())
                ))
                .collect(Collectors.toList());
        return new GetInterviewResponse(true, "Interviews found", dataList, null);
    }

    public GetInterviewResponse getInterviews(String candidateId) {

        Optional<CandidateDetails> optionalCandidate = candidateRepository.findById(candidateId);
        if (optionalCandidate.isEmpty()) {
            logger.error("No Candidate Found with CandidateId: {}", candidateId);
            throw new CandidateNotFoundException("Candidate Not Found With Id :" + candidateId + " to schedule Interview");
        }
        List<InterviewDetails> interviewDetails = interviewRepository.findInterviewsByCandidateId(candidateId);
        if (interviewDetails.isEmpty()) {
            logger.error("No Interviews Found For Candidate ID : {}", candidateId);
            throw new NoInterviewsFoundException("No Interviews Scheduled For CandidateId " + candidateId);
        } else {
            List<GetInterviewResponse.InterviewData> dataList = interviewDetails.stream()
                    .map(i -> new GetInterviewResponse.InterviewData(
                            i.getInterviewId(),
                            i.getJobId(),
                            i.getCandidateId(),
                            i.getFullName(),
                            i.getContactNumber(),
                            i.getCandidateEmailId(),
                            i.getUserEmail(),
                            i.getUserId(),
                            i.getInterviewDateTime(),
                            i.getDuration(),
                            i.getZoomLink(),
                            i.getTimestamp(),
                            i.getClientEmailList(),
                            i.getClientName(),
                            i.getInterviewLevel(),
                            latestInterviewStatusFromJson(i.getInterviewStatus()),
                            i.getIsPlaced(),
                            i.getRecruiterName(),
                            interviewRepository.findJobTitleByJobId(i.getJobId())
                    ))
                    .collect(Collectors.toList());
            return new GetInterviewResponse(true, "Interviews found", dataList, null);
        }
    }
    @Transactional
    public void deleteInterview(String candidateId, String jobId) {
        logger.info("Received request to remove scheduled interview details for candidateId: {}", candidateId);
        Optional<CandidateDetails> candidate = candidateRepository.findById(candidateId);
        if (candidate.isEmpty()) {
            logger.error("Invalid Candidate Id :{}", candidateId);
            throw new CandidateNotFoundException("No Candidate Found with Id :" + candidateId);
        }
        InterviewDetails interview = interviewRepository.findInterviewsByCandidateIdAndJobId(candidateId, jobId);
        if (interview == null) {
            logger.error("Candidate with ID {} not found in database", candidateId);
            throw new NoInterviewsFoundException("No Scheduled Interview found for candidate ID: " + candidateId + " for JobId: " + jobId);
        } else interviewRepository.delete(interview);

        logger.info("Scheduled interview details removed successfully for candidateId: {}", candidateId);
    }

    public GetInterviewResponse getInterviewsById(String interviewId) {

        Optional<InterviewDetails> optionalInterviewDetails = interviewRepository.findById(interviewId);
        if (optionalInterviewDetails.isEmpty()) {
            logger.error("Invalid Interview Id :{}", interviewId);
            throw new NoInterviewsFoundException("Invalid Interview Id " + interviewId);
        }
        InterviewDetails i = optionalInterviewDetails.get();

        logger.info("Latest Interview Status :{}",latestInterviewStatusFromJson(i.getInterviewStatus()));
        GetInterviewResponse.InterviewData payload = new GetInterviewResponse.InterviewData(
                i.getInterviewId(),
                i.getJobId(),
                i.getCandidateId(),
                i.getFullName(),
                i.getContactNumber(),
                i.getCandidateEmailId(),
                i.getUserEmail(),
                i.getUserId(),
                i.getInterviewDateTime(),
                i.getDuration(),
                i.getZoomLink(),
                i.getTimestamp(),
                i.getClientEmailList(),
                i.getClientName(),
                i.getInterviewLevel(),
                latestInterviewStatusFromJson(i.getInterviewStatus()),
                i.getIsPlaced(),
                i.getRecruiterName(),
                interviewRepository.findJobTitleByJobId(i.getJobId())
        );
        return new GetInterviewResponse(true, "Interview found", List.of(payload), null);
    }
    public InterviewResponseDto scheduleInterviewWithOutUserId(String candidateId, OffsetDateTime interviewDateTime, Integer duration,
                                                               String zoomLink, List<String> clientEmail,
                                                               String clientName, String interviewLevel, String externalInterviewDetails, String jobId, String fullName,
                                                               String contactNumber, String candidateEmailId, boolean skipNotification,String assignedTo,String comments) throws JsonProcessingException {

        System.out.println("Starting to schedule interview for userId: " + " and candidateId: " + candidateId);
        if (candidateId == null) {
            logger.error("Candidate Id Can not be null");
            throw new CandidateNotFoundException("Candidate ID cannot be null");
        }
        if (submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId, jobId) == null) {
            logger.error("Candidate Not Applied For a JobId: {}", jobId);
            throw new JobNotFoundException("Candidate Not Applied for Job " + jobId);
        }
        // Retrieve candidate details
        Optional<CandidateDetails> candidateDetails = candidateRepository.findById(candidateId);
        if (candidateDetails.isEmpty()) {
            logger.error("No Candidate found for Id {}", candidateId);
            new CandidateNotFoundException("No Candidate found for candidateId: " + candidateId);
        }
        InterviewDetails inti = interviewRepository.findByCandidateIdAndClientNameAndJobId(candidateId, clientName, jobId);
        if (inti != null) {
            logger.error("Interview Already Scheduled For Candidate ID :{} for Client {} and job {}", candidateId, clientName, jobId);
            throw new InterviewAlreadyScheduledException("An interview is already scheduled for candidate ID: " + candidateId);
        }
        InterviewDetails interviewDetails = new InterviewDetails();

        // Ensure no interview is already scheduled
        // Update candidate details with provided information
        interviewDetails.setClientEmailList(clientEmail);
        setDefaultEmailsIfMissing(interviewDetails);

        // Determine Interview Type if not provided
        if (interviewLevel == null || interviewLevel.isEmpty()) {
            interviewLevel = determineInterviewType(clientEmail, zoomLink);
        }
        interviewDetails.setInterviewLevel(interviewLevel);

            interviewDetails.setClientEmailList(clientEmail);
            interviewDetails.setZoomLink(zoomLink);

        interviewDetails.setCandidateId(candidateId);
        interviewDetails.setInterviewDateTime(interviewDateTime);
        interviewDetails.setDuration(duration);
        interviewDetails.setZoomLink(zoomLink);
        interviewDetails.setClientEmailList(clientEmail);
        interviewDetails.setClientName(clientName);
        interviewDetails.setInterviewLevel(interviewLevel);
        interviewDetails.setExternalInterviewDetails(externalInterviewDetails);
        interviewDetails.setFullName(fullName);
        interviewDetails.setContactNumber(contactNumber);
        interviewDetails.setCandidateEmailId(candidateEmailId);
        interviewDetails.setTimestamp(LocalDateTime.now());

        if(interviewLevel.equalsIgnoreCase("INTERNAL")){
            interviewDetails.setComments(comments);
        }

        String clientId = interviewRepository.findClientIdByClientName(clientName);
        if (clientId == null) throw new InvalidClientException("No Client With Name :" + clientName);

        interviewDetails.setClientId(clientId);
        String interviewId = candidateId + "_" + clientId + "_" + jobId;
        interviewDetails.setInterviewId(interviewId);
        interviewDetails.setJobId(jobId);

        Submissions submissions=submissionRepository.findByCandidate_CandidateIdAndJobId(candidateId,jobId);
        submissions.setStatus("MOVED TO INTERVIEW");
        // Set interview details
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode statusArray = objectMapper.createArrayNode();
        ObjectNode statusEntry = objectMapper.createObjectNode();
        statusEntry.put("stage", 1);
        statusEntry.put("status", "SCHEDULED");
        statusEntry.put("interviewLevel", interviewLevel);
        statusEntry.put("timestamp", OffsetDateTime.now().toString());
        statusArray.add(statusEntry);
        interviewDetails.setInterviewStatus(objectMapper.writeValueAsString(statusArray));

        if(assignedTo!=null && interviewLevel.equals("INTERNAL")){
            interviewDetails.setAssignedTo(assignedTo);
            interviewDetails.setCoordinatorName(interviewRepository.findUsernameByUserId(assignedTo));
        }
        else if(assignedTo!=null && !interviewLevel.equals("INTERNAL")){
            throw new InterviewNotScheduledException("For INTERNAL Interviews Only we Can Assign to Co-Ordinators");
        }else{
            interviewDetails.setAssignedTo(null);
            interviewDetails.setCoordinatorName(null);
        }
        // Save candidate details to the database
        try {
            interviewRepository.save(interviewDetails);
            System.out.println("Candidate saved successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Error while saving candidate data.", e);
        }
        if (!skipNotification ) {
            //sending mails
            String jobTitle = interviewRepository.findJobTitleByJobId(jobId);
            ZoneOffset istOffset = ZoneOffset.of("+05:30");
            OffsetDateTime interviewTimeInIST = (interviewDateTime != null) ? interviewDateTime.withOffsetSameInstant(istOffset) : null;

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            String formattedDate = (interviewTimeInIST != null) ? interviewTimeInIST.format(dateFormatter) : "N/A";
            String formattedTime = (interviewTimeInIST != null) ? interviewTimeInIST.format(timeFormatter) : "N/A";
            String subject = "Interview Scheduled for " + interviewDetails.getFullName();
              if(!interviewLevel.equalsIgnoreCase("INTERNAL")) {
                  emailService.sendEmailsToClients(interviewDetails.getClientEmailList(), subject, buildClientScheduleEmailBody(clientName,
                          formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
                  emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildCandidateScheduleEmailBody(interviewDetails.getFullName(),
                          formattedDate, formattedTime, duration, zoomLink, jobTitle, clientName));
              } else{
                  String coordinatorName=interviewRepository.findUsernameByUserId(assignedTo);
                  String coordinatorEmail=interviewRepository.findUserEmailByUserId(assignedTo);
                  logger.info("CoOrdinator Name {} and email {}",coordinatorName,coordinatorEmail);
                  emailService.sendEmailToCoordinator(coordinatorEmail, subject, buildCoordinatorScheduleEmailBody(coordinatorName,
                          formattedDate, formattedTime, duration, zoomLink, jobTitle, interviewDetails.getFullName()));
                  emailService.sendEmailToCandidate(interviewDetails.getCandidateEmailId(), subject, buildCandidateScheduleEmailBody(interviewDetails.getFullName(),
                          formattedDate, formattedTime, duration, zoomLink, jobTitle, clientName));
              }
        }
        // Prepare the response with interview details
        InterviewResponseDto.InterviewData data = new InterviewResponseDto.InterviewData(
                interviewDetails.getCandidateId(),
                interviewDetails.getUserEmail(),
                interviewDetails.getCandidateEmailId(),
                interviewDetails.getClientEmailList()
        );
        return new InterviewResponseDto(true, "Interview scheduled successfully and email notifications sent.", data, null);
    }
    public GetInterviewResponse getScheduledInterviewsByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate, String interviewLevelFilter) {

        logger.info("Fetching interviews for userId: {} between {} and {}", userId, startDate, endDate);
        if (endDate.isBefore(startDate)) {
            logger.error("End date is before start date: {} and {}", startDate, endDate);
            throw new DateRangeValidationException("End date must not be before the start date.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<GetInterviewResponse.InterviewData> payloadList = new ArrayList<>();

        String role = interviewRepository.findRoleByUserId(userId);

        if ("EMPLOYEE".equalsIgnoreCase(role)) {
            List<InterviewDetails> interviewDetails = interviewRepository.findScheduledInterviewsByUserIdAndDateRange(userId, startDateTime, endDateTime);
            logger.info("Fetched {} interviews for EMPLOYEE userId: {}", interviewDetails.size(), userId);
            payloadList = buildInterviewDataList(interviewDetails);

        } else if ("COORDINATOR".equalsIgnoreCase(role)) {
            List<InterviewDetails> interviewDetails = interviewRepository.findScheduledInterviewsByAssignedToAndDateRange(userId, startDateTime, endDateTime);
            logger.info("Fetched {} interviews for COORDINATOR userId: {}", interviewDetails.size(), userId);
            payloadList = buildInterviewDataList(interviewDetails);

        } else if ("SUPERADMIN".equalsIgnoreCase(role)) {
            List<InterviewDetails> interviewDetails = interviewRepository.findScheduledInterviewsByDateOnly(startDate, endDate);
            logger.info("Fetched {} interviews for SUPERADMIN userId: {}", interviewDetails.size(), userId);
            payloadList = buildInterviewDataList(interviewDetails);

        }
        else if ("BDM".equalsIgnoreCase(role)) {
            List<Tuple> bdmInterviews = interviewRepository.findScheduledInterviewsByBdmUserIdAndDateRange(userId, startDateTime, endDateTime);
            logger.info("Fetched {} interviews for BDM userId: {}", bdmInterviews.size(), userId);

            for (Tuple tuple : bdmInterviews) {
                String interviewDateTimeStr = tuple.get("interview_date_time", String.class);
                OffsetDateTime interviewDateTime = null;

                if (interviewDateTimeStr != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(interviewDateTimeStr, formatter);
                    interviewDateTime = localDateTime.atOffset(ZoneOffset.ofHoursMinutes(5, 30)); // IST
                }

                if (interviewDateTime != null) {
                    String latestInterviewStatus = latestInterviewStatusFromJson(tuple.get("interview_status", String.class));
                    String timestampStr = tuple.get("timestamp", String.class);
                    LocalDateTime timestamp = timestampStr != null
                            ? LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                            : null;

                    ObjectMapper objectMapper = new ObjectMapper();
                    List<String> clientEmails = new ArrayList<>();
                    try {
                        clientEmails = objectMapper.readValue(tuple.get("client_email", String.class), new TypeReference<>() {
                        });
                    } catch (JsonProcessingException e) {
                        logger.warn("Failed to parse client emails for interview ID {}", tuple.get("interview_id", String.class));
                    }

                    payloadList.add(new GetInterviewResponse.InterviewData(
                            tuple.get("interview_id", String.class),
                            tuple.get("job_id", String.class),
                            tuple.get("candidate_id", String.class),
                            tuple.get("full_name", String.class),
                            tuple.get("contact_number", String.class),
                            tuple.get("candidate_email_id", String.class),
                            tuple.get("user_email", String.class),
                            tuple.get("user_id", String.class),
                            interviewDateTime,
                            tuple.get("duration", Integer.class),
                            tuple.get("zoom_link", String.class),
                            timestamp,
                            clientEmails,
                            tuple.get("client_name", String.class),
                            tuple.get("interview_level", String.class),
                            latestInterviewStatus,
                            tuple.get("is_placed", Boolean.class),
                            tuple.get("recruiterName", String.class),
                            interviewRepository.findJobTitleByJobId(tuple.get("job_id", String.class))
                    ));
                }
            }
        } else {
            logger.error("Unsupported role {} for userId {}", role, userId);
            throw new UnsupportedOperationException("Only EMPLOYEE, COORDINATOR, BDM, and SUPERADMIN roles are supported.");
        }

        if (interviewLevelFilter != null && !"ALL".equalsIgnoreCase(interviewLevelFilter)) {
            String level = interviewLevelFilter.trim().toLowerCase();
            payloadList = payloadList.stream()
                    .filter(dto -> dto.getInterviewLevel() != null && dto.getInterviewLevel().trim().toLowerCase().startsWith(level))
                    .collect(Collectors.toList());
            logger.info("Filtered interviews by interviewLevel '{}': {}", level.toUpperCase(), payloadList.size());
        }

        logger.info("Total interviews returned in response for userId {} with role {}: {}", userId, role, payloadList.size());
        return new GetInterviewResponse(true, "Interviews found", payloadList, null);
    }

    private List<GetInterviewResponse.InterviewData> buildInterviewDataList(List<InterviewDetails> interviewDetails) {
        return interviewDetails.stream()
                .filter(i -> i.getInterviewDateTime() != null)
                .map(i -> new GetInterviewResponse.InterviewData(
                        i.getInterviewId(),
                        i.getJobId(),
                        i.getCandidateId(),
                        i.getFullName(),
                        i.getContactNumber(),
                        i.getCandidateEmailId(),
                        i.getUserEmail(),
                        i.getUserId(),
                        i.getInterviewDateTime(),
                        i.getDuration(),
                        i.getZoomLink(),
                        i.getTimestamp(),
                        i.getClientEmailList(),
                        i.getClientName(),
                        i.getInterviewLevel(),
                        latestInterviewStatusFromJson(i.getInterviewStatus()),
                        i.getIsPlaced(),
                        i.getRecruiterName(),
                        interviewRepository.findJobTitleByJobId(i.getJobId())
                ))
                .collect(Collectors.toList());
    }

    public static String latestInterviewStatusFromJson(String interviewStatusJson) {
        String latestInterviewStatus = null;
        ObjectMapper objectMapper = new ObjectMapper();

        if (interviewStatusJson != null && !interviewStatusJson.trim().isEmpty()) {
            try {
                if (interviewStatusJson.trim().startsWith("{") || interviewStatusJson.trim().startsWith("[")) {
                    List<Map<String, Object>> statusHistory = objectMapper.readValue(interviewStatusJson, List.class);

                    if (!statusHistory.isEmpty()) {
                        Optional<Map<String, Object>> latestStatus = statusHistory.stream()
                                .filter(entry -> entry.get("timestamp") != null)
                                .max(Comparator.comparing(entry -> Instant.parse((String) entry.get("timestamp"))));

                        if (latestStatus.isPresent()) {
                            latestInterviewStatus = (String) latestStatus.get().get("status");
                        }
                    }
                } else {
                    latestInterviewStatus = interviewStatusJson;
                }
            } catch (JsonParseException e) {
                System.err.println("Error parsing interview status JSON: Invalid JSON format detected.");
                latestInterviewStatus = interviewStatusJson;
            } catch (IOException e) {
                System.err.println("Error reading interview status: " + e.getMessage());
            }
        }

        return latestInterviewStatus;
    }


    private String buildCandidateScheduleEmailBody(String recipientName, String formattedDate, String formattedTime,
                                                   int formattedDuration, String formattedZoomLink, String jobTitle,
                                                   String clientName) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>Hope you are doing well!</p>"
                        + "<p>Thank you for your interest in the position <b>%s</b> for our client <b>%s</b>.</p>"
                        + "<p>We're pleased to inform you that your profile has been shortlisted for screening.</p>"
                        + "<p>Interview Details:</p>"
                        + "<ul>"
                        + "<li><b>Date:</b> %s</li>"
                        + "<li><b>Time:</b> %s</li>"
                        + "<li><b>Duration:</b> Approx. %s min</li>"
                        + (formattedZoomLink != null && !formattedZoomLink.isEmpty()
                        ? "<li><b>Join Zoom Meeting:</b> " + formattedZoomLink + "</li>" : "")
                        + "</ul>"
                        + "<p>Kindly confirm your availability by replying to this email.</p>"
                        + "<p>Best regards,</p>"
                        + "<p>The Interview Team</p>",
                recipientName, jobTitle, clientName, formattedDate, formattedTime, formattedDuration);
    }

    private String buildClientScheduleEmailBody(String clientName, String formattedDate, String formattedTime,
                                                int formattedDuration, String formattedZoomLink, String jobTitle,
                                                String candidateName) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>Hope you are doing well!</p>"
                        + "<p>This is to inform you that an interview has been scheduled for the position <b>%s</b>.</p>"
                        + "<p>Candidate Name: <b>%s</b></p>"
                        + "<p>Interview Details:</p>"
                        + "<ul>"
                        + "<li><b>Date:</b> %s</li>"
                        + "<li><b>Time:</b> %s</li>"
                        + "<li><b>Duration:</b> Approx. %s min</li>"
                        + (formattedZoomLink != null && !formattedZoomLink.isEmpty()
                        ? "<li><b>Join Zoom Meeting:</b> " + formattedZoomLink + "</li>" : "")
                        + "</ul>"
                        + "<p>Please let us know if you need any further information.</p>"
                        + "<p>Best regards,</p>"
                        + "<p>The Coordination Team</p>",
                clientName, jobTitle, candidateName, formattedDate, formattedTime, formattedDuration);
    }
    private String buildCoordinatorScheduleEmailBody(String coordinatorName, String formattedDate, String formattedTime,
                                                int formattedDuration, String formattedZoomLink, String jobTitle,
                                                String candidateName) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>Hope you are doing well!</p>"
                        + "<p>This is to inform you that an interview has been scheduled for the position <b>%s</b>.</p>"
                        + "<p>Candidate Name: <b>%s</b></p>"
                        + "<p>Interview Details:</p>"
                        + "<ul>"
                        + "<li><b>Date:</b> %s</li>"
                        + "<li><b>Time:</b> %s</li>"
                        + "<li><b>Duration:</b> Approx. %s min</li>"
                        + (formattedZoomLink != null && !formattedZoomLink.isEmpty()
                        ? "<li><b>Join Zoom Meeting:</b> " + formattedZoomLink + "</li>" : "")
                        + "</ul>"
                        + "<p>Please let us know if you need any further information.</p>"
                        + "<p>Best regards,</p>"
                        + "<p>The Coordination Team</p>",
                coordinatorName, jobTitle, candidateName, formattedDate, formattedTime, formattedDuration);
    }

    private String buildUserScheduleEmailBody(String userName, String clientName, String formattedDate,
                                              String formattedTime, int formattedDuration, String formattedZoomLink,
                                              String jobTitle, String candidateName) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>Hope you're doing well!</p>"
                        + "<p>This is to confirm that the interview for the position <b>%s</b> has been scheduled with the client <b>%s</b>.</p>"
                        + "<p>Candidate Name: <b>%s</b></p>"
                        + "<p>Interview Details:</p>"
                        + "<ul>"
                        + "<li><b>Date:</b> %s</li>"
                        + "<li><b>Time:</b> %s</li>"
                        + "<li><b>Duration:</b> Approx. %s min</li>"
                        + (formattedZoomLink != null && !formattedZoomLink.isEmpty()
                        ? "<li><b>Zoom Link:</b> " + formattedZoomLink + "</li>" : "")
                        + "</ul>"
                        + "<p>The client has been informed. Please monitor for the candidate's confirmation and be available for any coordination if needed.</p>"
                        + "<p>Best regards,</p>"
                        + "<p>The Scheduling System</p>",
                userName, jobTitle, clientName, candidateName, formattedDate, formattedTime, formattedDuration);
    }

    private String buildUpdateCandidateEmailBody(String candidateName, String formattedDate, String formattedTime,
                                                 String formattedDuration, String formattedZoomLink, String jobTitle,
                                                 String clientName) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>Your interview for the position <b>%s</b> with our client <b>%s</b> has been rescheduled.</p>"
                        + "<p>Updated Interview Details:</p>"
                        + "<ul>"
                        + "<li><b>New Date:</b> %s</li>"
                        + "<li><b>New Time:</b> %s</li>"
                        + "<li><b>Duration:</b> Approx. %s</li>"
                        + (formattedZoomLink != null && !formattedZoomLink.isEmpty()
                        ? "<li><b>New Zoom Link:</b> " + formattedZoomLink + "</li>" : "")
                        + "</ul>"
                        + "<p>Please confirm your availability.</p>"
                        + "<p>Best regards,<br>The Interview Team</p>",
                candidateName, jobTitle, clientName, formattedDate, formattedTime, formattedDuration);
    }

    private String buildUpdateClientEmailBody(String clientName, String formattedDate, String formattedTime,
                                              String formattedDuration, String formattedZoomLink, String jobTitle,
                                              String candidateName) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>The interview for the position <b>%s</b> has been rescheduled.</p>"
                        + "<p>Candidate Name: <b>%s</b></p>"
                        + "<p>Updated Interview Details:</p>"
                        + "<ul>"
                        + "<li><b>New Date:</b> %s</li>"
                        + "<li><b>New Time:</b> %s</li>"
                        + "<li><b>Duration:</b> Approx. %s</li>"
                        + (formattedZoomLink != null && !formattedZoomLink.isEmpty()
                        ? "<li><b>New Zoom Link:</b> " + formattedZoomLink + "</li>" : "")
                        + "</ul>"
                        + "<p>Let us know if you need further details.</p>"
                        + "<p>Best regards,<br>The Coordination Team</p>",
                clientName, jobTitle, candidateName, formattedDate, formattedTime, formattedDuration);
    }

    private String buildUpdateUserEmailBody(String userName, String clientName, String formattedDate,
                                            String formattedTime, String formattedDuration, String formattedZoomLink,
                                            String jobTitle, String candidateName) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>The interview for the position <b>%s</b> with client <b>%s</b> has been rescheduled.</p>"
                        + "<p>Candidate Name: <b>%s</b></p>"
                        + "<p>Updated Interview Details:</p>"
                        + "<ul>"
                        + "<li><b>New Date:</b> %s</li>"
                        + "<li><b>New Time:</b> %s</li>"
                        + "<li><b>Duration:</b> Approx. %s</li>"
                        + (formattedZoomLink != null && !formattedZoomLink.isEmpty()
                        ? "<li><b>New Zoom Link:</b> " + formattedZoomLink + "</li>" : "")
                        + "</ul>"
                        + "<p>The client has been informed. Please coordinate as needed.</p>"
                        + "<p>Best regards,<br>The Scheduling System</p>",
                userName, jobTitle, clientName, candidateName, formattedDate, formattedTime, formattedDuration);
    }

    public GetInterviewResponse getScheduledInterviewsByDateOnly(LocalDate startDate, LocalDate endDate) {

        logger.info("Fetching interviews between {} and {}", startDate, endDate);
        if (endDate.isBefore(startDate)) {
            logger.error("End date is before start date: {} and {}", startDate, endDate);
            throw new DateRangeValidationException("End date must not be before the start date.");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        // Log before fetching data
        logger.info("Fetching scheduled interviews for userId: {} between {} and {}", startDateTime, endDateTime);
        List<InterviewDetails> interviewDetails = interviewRepository.findScheduledInterviewsByDateOnly(startDate, endDate);

        // Log if no candidates found
        if (interviewDetails.isEmpty()) {
            logger.warn("No interviews found between {} and {}", startDate, endDate);
            throw new CandidateNotFoundException("No interviews found between " + startDate + " and " + endDate);
        }
        // Log if interviews found
        logger.info("Fetched {} interviews for userId: {} between {} and {}", interviewDetails.size(), startDate, endDate);

        List<GetInterviewResponse.InterviewData> payloadList = interviewDetails.stream()
                .map(i -> new GetInterviewResponse.InterviewData(
                        i.getInterviewId(),
                        i.getJobId(),
                        i.getCandidateId(),
                        i.getFullName(),
                        i.getContactNumber(),
                        i.getCandidateEmailId(),
                        i.getUserEmail(),
                        i.getUserId(),
                        i.getInterviewDateTime(),
                        i.getDuration(),
                        i.getZoomLink(),
                        i.getTimestamp(),
                        i.getClientEmailList(),
                        i.getClientName(),
                        i.getInterviewLevel(),
                        latestInterviewStatusFromJson(i.getInterviewStatus()),
                        i.getIsPlaced(),
                        i.getRecruiterName(),
                        interviewRepository.findJobTitleByJobId(i.getJobId())
                ))
                .collect(Collectors.toList());
        return new GetInterviewResponse(true, "Interviews found", payloadList, null);
    }
    public List<GetInterviewResponseDto> getAllScheduledInterviewsByUserId(String userId, String interviewLevelFilter) throws JsonProcessingException {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

        logger.info("Fetching interviews for userId: {} between {} and {}", userId, startOfMonth, endOfMonth);

        String role = interviewRepository.findRoleByUserId(userId);
        logger.info("User role for userId {}: {}", userId, role);

        List<GetInterviewResponseDto> response = new ArrayList<>();

        switch (role.toUpperCase()) {
            case "EMPLOYEE":
                List<InterviewDetails> employeeInterviews = interviewRepository.findScheduledInterviewsByUserIdAndDateRange(userId, startDateTime, endDateTime);
                logger.info("Fetched {} interviews for EMPLOYEE userId: {}", employeeInterviews.size(), userId);
                for (InterviewDetails interview : employeeInterviews) {
                    if (interview.getInterviewDateTime() != null) {
                        response.add(toDto(interview));
                    }
                }
                break;

            case "COORDINATOR":
                List<InterviewDetails> coordinatorInterviews = interviewRepository.findScheduledInterviewsByAssignedToAndDateRange(userId, startDateTime, endDateTime);
                logger.info("Fetched {} interviews for COORDINATOR userId: {}", coordinatorInterviews.size(), userId);
                for (InterviewDetails interview : coordinatorInterviews) {
                    if (interview.getInterviewDateTime() != null) {
                        response.add(toDto(interview));
                    }
                }
                break;

            case "BDM":
                List<Tuple> bdmInterviews = interviewRepository.findScheduledInterviewsByBdmUserIdAndDateRange(userId, startDateTime, endDateTime);
                logger.info("Fetched {} interviews for BDM userId: {}", bdmInterviews.size(), userId);
                for (Tuple tuple : bdmInterviews) {
                    String interviewDateTimeStr = tuple.get("interview_date_time", String.class);
                    OffsetDateTime interviewDateTime = null;
                    if (interviewDateTimeStr != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        LocalDateTime localDateTime = LocalDateTime.parse(interviewDateTimeStr, formatter);
                        interviewDateTime = localDateTime.atOffset(ZoneOffset.ofHoursMinutes(5, 30));
                    }

                    if (interviewDateTime != null) {
                        String latestInterviewStatus = latestInterviewStatusFromJson(tuple.get("interview_status", String.class));
                        String timestampStr = tuple.get("timestamp", String.class);
                        LocalDateTime timestamp = timestampStr != null ? LocalDateTime.parse(timestampStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) : null;

                        ObjectMapper objectMapper = new ObjectMapper();
                        List<String> clientEmails = objectMapper.readValue(tuple.get("client_email", String.class), new TypeReference<>() {});

                        response.add(new GetInterviewResponseDto(
                                tuple.get("interview_id", String.class),
                                tuple.get("job_id", String.class),
                                tuple.get("candidate_id", String.class),
                                tuple.get("full_name", String.class),
                                tuple.get("contact_number", String.class),
                                tuple.get("candidate_email_id", String.class),
                                tuple.get("user_email", String.class),
                                tuple.get("user_id", String.class),
                                interviewDateTime,
                                tuple.get("duration", Integer.class),
                                tuple.get("zoom_link", String.class),
                                timestamp,
                                clientEmails,
                                tuple.get("client_name", String.class),
                                tuple.get("interview_level", String.class),
                                latestInterviewStatus,
                                tuple.get("recruiterName", String.class),
                                tuple.get("is_placed", Boolean.class),
                                tuple.get("technlogy",String.class),
                                tuple.get("internalFeedback",String.class),
                                tuple.get("comments",String.class)
                        ));
                    }
                }
                break;

            case "SUPERADMIN":
                List<InterviewDetails> allInterviews = interviewRepository.findScheduledInterviewsByDateOnly(startOfMonth, endOfMonth);
                logger.info("Fetched {} interviews for SUPERADMIN", allInterviews.size());
                for (InterviewDetails interview : allInterviews) {
                    if (interview.getInterviewDateTime() != null) {
                        response.add(toDto(interview));
                    }
                }
                break;

            default:
                logger.error("Unsupported role {} for userId {}", role, userId);
                throw new UnsupportedOperationException("Only EMPLOYEE, COORDINATOR, BDM, and SUPERADMIN roles are supported.");
        }

        if (interviewLevelFilter != null && !interviewLevelFilter.equalsIgnoreCase("ALL")) {
            String level = interviewLevelFilter.trim().toLowerCase();
            response = response.stream()
                    .filter(dto -> dto.getInterviewLevel() != null && dto.getInterviewLevel().trim().toLowerCase().startsWith(level))
                    .collect(Collectors.toList());
            logger.info("Filtered interviews by interviewLevel '{}': {}", level.toUpperCase(), response.size());
        }

        logger.info("Total interviews returned in response for userId {} with role {}: {}", userId, role, response.size());
        return response;
    }



    private GetInterviewResponseDto toDto(InterviewDetails interview) {
        String technology = (interviewRepository.findJobTitleByJobId(interview.getJobId()));

        return new GetInterviewResponseDto(
                interview.getInterviewId(),
                interview.getJobId(),
                interview.getCandidateId(),
                interview.getFullName(),
                interview.getContactNumber(),
                interview.getCandidateEmailId(),
                interview.getUserEmail(),
                interview.getUserId(),
                interview.getInterviewDateTime(),
                interview.getDuration(),
                interview.getZoomLink(),
                interview.getTimestamp(),
                interview.getClientEmailList(),
                interview.getClientName(),
                interview.getInterviewLevel(),
                latestInterviewStatusFromJson(interview.getInterviewStatus()),
                interview.getRecruiterName(),
                interview.getIsPlaced(),
                technology,
                interview.getInternalFeedback(),
                interview.getComments()
        );
    }

    public TeamleadInterviewsDTO getTeamleadScheduledInterviewsByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        // 1. Validate the date range
        if (startDate == null || endDate == null) {
            throw new DateRangeValidationException("Start date and End date must not be null.");
        }
        if (endDate.isBefore(startDate)) {
            throw new DateRangeValidationException("End date cannot be before start date.");
        }

        // 2. Log the date range
        logger.info("Fetching interviews for teamlead with userId: {} between {} and {}", userId, startDate, endDate);

//        // 3. Prepare date range (convert LocalDate to LocalDateTime for query accuracy)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 4. Fetch self and team interviews within the date range
        List<InterviewDetails> selfInterviewsRaw = interviewRepository.findSelfScheduledInterviewsByTeamleadAndDateRange(userId, startDateTime, endDateTime);
        List<InterviewDetails> teamInterviewsRaw = interviewRepository.findTeamScheduledInterviewsByTeamleadAndDateRange(userId, startDateTime, endDateTime);

        // 5. Log the number of self and team interviews
        logger.info("Fetched {} self interviews for teamlead with userId: {} between {} and {}",
                selfInterviewsRaw.size(), userId, startDate, endDate);
        logger.info("Fetched {} team interviews for teamlead with userId: {} between {} and {}",
                teamInterviewsRaw.size(), userId, startDate, endDate);

        // 6. Handle empty result


        // 7. Parse the raw data into response DTOs using the updated GetInterviewResponseDto
        List<GetInterviewResponseDto> selfInterviews = parseInterviewCandidates(selfInterviewsRaw);
        List<GetInterviewResponseDto> teamInterviews = parseInterviewCandidates(teamInterviewsRaw);

        // 8. Return the DTO with both lists
        return new TeamleadInterviewsDTO(selfInterviews, teamInterviews);
    }
    private List<GetInterviewResponseDto> parseInterviewCandidates(List<InterviewDetails> interviews) {
        List<GetInterviewResponseDto> response = new ArrayList<>();

        for (InterviewDetails interview : interviews) {
            if (interview.getInterviewDateTime() == null) continue;
            String latestInterviewStatus = latestInterviewStatusFromJson(interview.getInterviewStatus());

            response.add(new GetInterviewResponseDto(
                    interview.getInterviewId(),
                    interview.getJobId(),
                    interview.getCandidateId(),
                    interview.getFullName(),
                    interview.getContactNumber(),
                    interview.getCandidateEmailId(),
                    interview.getUserEmail(),
                    interview.getUserId(),
                    interview.getInterviewDateTime(),
                    interview.getDuration(),
                    interview.getZoomLink(),
                    interview.getTimestamp(),
                    interview.getClientEmailList(),
                    interview.getClientName(),
                    interview.getInterviewLevel(),
                    latestInterviewStatus,
                    interview.getRecruiterName(),
                    interview.getIsPlaced(),
                    interviewRepository.findJobTitleByJobId(interview.getJobId()),
                    interview.getInternalFeedback(),
                    interview.getComments()
            ));
        }
        return response;
    }
    public TeamleadInterviewsDTO getTeamleadScheduledInterviews(String userId) {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the start and end date for the current month
        LocalDate startOfMonth = currentDate.withDayOfMonth(1);  // First day of the current month
        LocalDate endOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());  // Last day of the current month

        // Convert LocalDate to LocalDateTime for query compatibility (starting at the beginning and end of the day)
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

        // Fetch self and team interviews for the current month using the updated queries
        List<InterviewDetails> selfInterviewsRaw = interviewRepository.findSelfScheduledInterviewsByTeamleadAndDateRange(userId, startDateTime, endDateTime);
        List<InterviewDetails> teamInterviewsRaw = interviewRepository.findTeamScheduledInterviewsByTeamleadAndDateRange(userId, startDateTime, endDateTime);


        // Log the fetched data for monitoring purposes
        logger.info("Fetched {} self interviews for teamlead with userId: {} between {} and {}",
                selfInterviewsRaw.size(), userId, startDateTime, endDateTime);
        logger.info("Fetched {} team interviews for teamlead with userId: {} between {} and {}",
                teamInterviewsRaw.size(), userId, startDateTime, endDateTime);

        // Parse the raw data into response DTOs
        List<GetInterviewResponseDto> selfInterviews = parseInterviewCandidates(selfInterviewsRaw);
        List<GetInterviewResponseDto> teamInterviews = parseInterviewCandidates(teamInterviewsRaw);

        // Return the DTO with both lists
        return new TeamleadInterviewsDTO(selfInterviews, teamInterviews);
    }

    public InterviewSlotsDto getInterviewSlots(String userId){

        if(candidateRepository.findUserNameByUserId(userId).isEmpty())
            throw new UserNotFoundException("No User Found With Id :"+userId);
//        String role=interviewRepository.findRoleByUserId(userId);
//        if(!role.equalsIgnoreCase("COORDINATOR"))
//            throw new UserNotFoundException("Only COORDINATORS are Allowed");
        List<InterviewDetails> interviewDetailsList=interviewRepository.findByAssignedTo(userId);
        InterviewSlotsDto dto=new InterviewSlotsDto();
        dto.setUserId(userId);
        List<InterviewSlotsDto.InterviewDateWithDuration> dateTimeList=new ArrayList<>();

        interviewDetailsList.stream()
                .forEach((interview)-> {
                    InterviewSlotsDto.InterviewDateWithDuration timeWithDuration=new InterviewSlotsDto.InterviewDateWithDuration();
                    timeWithDuration.setInterviewDateTime(interview.getInterviewDateTime().withOffsetSameInstant(ZoneOffset.of("+05:30")));
                    timeWithDuration.setDuration(interview.getDuration());
                    dateTimeList.add(timeWithDuration);
                });
        dto.setBookedSlots(dateTimeList);
        return dto;
    }

    public InterviewResponseDto updateInterviewByCoordinator(String coordinatorId,String interviewId,CoordinatorInterviewUpdateDto  dto){

        InterviewDetails interview=interviewRepository.findByInterviewIdAndAssignedTo(interviewId,coordinatorId);

        if(interview==null) throw new NoInterviewsFoundException("No Interview Found InterviewId"+interviewId+" for CoordinatorId "+coordinatorId);

         else {
             interview.setInternalFeedback(dto.getInternalFeedBack());

        }
          interviewRepository.save(interview);


        String jobTitle = interviewRepository.findJobTitleByJobId(interview.getJobId());
        String subject="Candidate Selected For Internal Level";
        String userEmailId = interview.getUserEmail();
        Optional<CandidateDetails> optionalCandidate=candidateRepository.findById(interview.getCandidateId());
        CandidateDetails candidate=optionalCandidate.get();
        String userName=candidateRepository.findUserNameByUserId(interview.getUserId());
        if (userEmailId == null || userEmailId.isEmpty()) {
            logger.error("User email is null or empty for candidateId: {}", interview.getCandidateId());
        } else {
            if (!dto.isSkipNotification()) {
                try {
                        emailService.sendEmailToUser(interview.getUserEmail(),subject,buildFeedbackEmailBody(
                                userName,candidate.getFullName(),interview.getJobId(),
                                        dto.getInternalFeedBack()));
                } catch (Exception e) {
                    logger.error("Error processing interview notifications: {}", e.getMessage(), e);
                }
            }
        }

        return new InterviewResponseDto(
                true,
                dto.isSkipNotification() ? "Interview updated successfully." : "Interview updated successfully and notifications sent.",
                new InterviewResponseDto.InterviewData(
                        interview.getCandidateId(),
                        interview.getUserEmail(),
                        interview.getCandidateEmailId(),
                        interview.getClientEmailList()
                ),
                null
        );
    }
    private String buildFeedbackEmailBody(
            String userName,
            String candidateName,
            String jobTitle,
            String feedbackComments
    ) {
        return String.format(
                "<p>Hello %s,</p>"
                        + "<p>You have provided feedback for the interview of candidate <b>%s</b> "
                        + "for the position <b>%s</b>.</p>"
                        + "<p><b>Feedback Summary:</b></p>"
                        + "<ul>"
                        + "<li><b>Comments:</b> %s</li>"
                        + "</ul>"
                        + "<p>Thank you for your input.</p>"
                        + "<p>Best regards,<br>The Scheduling System</p>",
                userName, candidateName, jobTitle,
                feedbackComments != null ? feedbackComments : "N/A"
        );
    }

    public List<CoordinatorInterviewDto> getCoordinatorInterviews(String userId){

        List<InterviewDetails> interviews=interviewRepository.findByAssignedTo(userId);

        List<CoordinatorInterviewDto> response=interviews.stream()
                .map(interview -> {
                    String technology=interviewRepository.findJobTitleByJobId(interview.getJobId());
                   return InterviewService.convertIntoDto(interview,technology);
                })
                .collect(Collectors.toList());

        return response;
    }

    public static CoordinatorInterviewDto convertIntoDto(InterviewDetails interviewDetails,String technology){

        CoordinatorInterviewDto dto=new CoordinatorInterviewDto();

        dto.setInterviewId(interviewDetails.getInterviewId());
        dto.setFullName(interviewDetails.getFullName());
        dto.setInterviewLevel(interviewDetails.getInterviewLevel());
        dto.setInterviewStatus(latestInterviewStatusFromJson(interviewDetails.getInterviewStatus()));
        dto.setCandidateId(interviewDetails.getCandidateId());
        dto.setCandidateEmailId(interviewDetails.getCandidateEmailId());
        dto.setContactNumber(interviewDetails.getContactNumber());
        dto.setClientName(interviewDetails.getClientName());
        dto.setDuration(interviewDetails.getDuration());
        dto.setInterviewDateTime(interviewDetails.getInterviewDateTime().toLocalDateTime());
        dto.setJobId(interviewDetails.getJobId());
        dto.setComments(interviewDetails.getComments());
        dto.setInternalFeedback(interviewDetails.getInternalFeedback());
        dto.setUserEmail(interviewDetails.getUserEmail());
        dto.setRecruiterName(interviewDetails.getRecruiterName());
        dto.setExternalInterviewDetails(interviewDetails.getExternalInterviewDetails());
        dto.setZoomLink(interviewDetails.getZoomLink());
        dto.setUserId(interviewDetails.getUserId());
        dto.setTechnology(technology);
        return dto;
    }

}