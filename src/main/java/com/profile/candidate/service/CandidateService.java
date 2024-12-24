package com.profile.candidate.service;

import com.profile.candidate.dto.CandidateGetResponseDto;
import com.profile.candidate.dto.CandidateResponseDto;
import com.profile.candidate.dto.GetInterviewResponseDto;
import com.profile.candidate.dto.InterviewResponseDto;
import com.profile.candidate.exceptions.CandidateAlreadyExistsException;
import com.profile.candidate.exceptions.CandidateNotFoundException;
import com.profile.candidate.model.CandidateDetails;
import com.profile.candidate.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    // Method to submit a candidate profile
    public CandidateResponseDto submitCandidate(CandidateDetails candidateDetails) {
        // Validate input fields
        validateCandidateDetails(candidateDetails);

        // Check for duplicates
        checkForDuplicates(candidateDetails);

        // Optionally set userEmail and clientEmail if not already set
        setDefaultEmailsIfMissing(candidateDetails);

        // Save the candidate details to the database
        CandidateDetails savedCandidate = candidateRepository.save(candidateDetails);

        // Return response DTO
        return new CandidateResponseDto(
                "Candidate profile submitted successfully.",
                savedCandidate.getCandidateId(),
                savedCandidate.getUserId(),
                savedCandidate.getJobId()
        );
    }

    // Validate required candidate fields
    private void validateCandidateDetails(CandidateDetails candidateDetails) {
        if (candidateDetails.getFullName() == null || candidateDetails.getFullName().trim().isEmpty()) {
            throw new CandidateAlreadyExistsException("Full Name is required and cannot be empty.");
        }

        if (candidateDetails.getEmailId() == null || !candidateDetails.getEmailId().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new CandidateAlreadyExistsException("Invalid email format.");
        }

        if (candidateDetails.getContactNumber() == null || !candidateDetails.getContactNumber().matches("\\d{10}")) {
            throw new CandidateAlreadyExistsException("Contact number must be 10 digits.");
        }


    }

    // Check for duplicate candidate based on Full Name, Email ID, and Contact Number
    private void checkForDuplicates(CandidateDetails candidateDetails) {
        // Check for duplicate Full Name
        Optional<CandidateDetails> existingFullName = candidateRepository.findByFullName(candidateDetails.getFullName());
        if (existingFullName.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same full name already exists: " + existingFullName.get().getFullName());
        }

        // Check for duplicate Email ID
        Optional<CandidateDetails> existingEmailId = candidateRepository.findByEmailId(candidateDetails.getEmailId());
        if (existingEmailId.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same email ID already exists: " + existingEmailId.get().getEmailId());
        }

        // Check for duplicate Contact Number
        Optional<CandidateDetails> existingContactNumber = candidateRepository.findByContactNumber(candidateDetails.getContactNumber());
        if (existingContactNumber.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same contact number already exists: " + existingContactNumber.get().getContactNumber());
        }
    }

    // Set default values for userEmail and clientEmail if not provided
    private void setDefaultEmailsIfMissing(CandidateDetails candidateDetails) {
        if (candidateDetails.getUserEmail() == null) {
            candidateDetails.setUserEmail(candidateDetails.getUserEmail());  // Set to default or handle differently
        }

        if (candidateDetails.getClientEmail() == null) {
            candidateDetails.setClientEmail(candidateDetails.getClientEmail());  // Set to default or handle differently
        }
    }

    // Method to get all candidate submissions
    public List<CandidateDetails> getSubmissions() {
        // Retrieve all candidates from the repository
        return candidateRepository.findAll();
    }

    // Method to get candidate submissions by userId
    public List<CandidateGetResponseDto> getSubmissionsByUserId(String userId) {
        // Retrieve candidates by userId from the repository
        List<CandidateDetails> candidates = candidateRepository.findByUserId(userId);

        // If no candidates are found, throw a CandidateNotFoundException
        if (candidates.isEmpty()) {
            throw new CandidateNotFoundException("No submissions found for userId: " + userId);
        }

        // Map the list of CandidateDetails to List<CandidateGetResponseDto>
        List<CandidateGetResponseDto> candidateDtos = candidates.stream()
                .map(CandidateGetResponseDto::new)  // Convert each CandidateDetails to CandidateGetResponseDto
                .collect(Collectors.toList());

        // Return the list of CandidateGetResponseDto
        return candidateDtos;
    }

    // Method to schedule an interview for a candidate

    public InterviewResponseDto scheduleInterview(String userId, LocalDateTime interviewDateTime, Integer duration,
                                                  String zoomLink, String userEmail, String clientEmail) {
        // Find candidates by userId (returns a List)
        List<CandidateDetails> candidates = candidateRepository.findByUserId(userId);

        // Check if the list is empty
        if (candidates.isEmpty()) {
            throw new CandidateNotFoundException("Candidate not found for userId: " + userId);
        }

        // Assuming you're working with the first candidate in the list
        CandidateDetails candidate = candidates.get(0);

        // Set userEmail and clientEmail from interview request if provided
        if (userEmail != null && !userEmail.isEmpty()) {
            candidate.setUserEmail(userEmail); // Set the userEmail passed from the interview DTO
        }
        if (clientEmail != null && !clientEmail.isEmpty()) {
            candidate.setClientEmail(clientEmail); // Set the clientEmail passed from the interview DTO
        }

        // Ensure userEmail and clientEmail are set (default them if missing)
        setDefaultEmailsIfMissing(candidate);

        // Update interview fields
        candidate.setInterviewDateTime(interviewDateTime);
        candidate.setDuration(duration);
        candidate.setTimestamp(LocalDateTime.now());
        candidate.setZoomLink(zoomLink);

        // Save the updated candidate details to the database
        candidateRepository.save(candidate);

        // Create the payload for the response
        InterviewResponseDto.InterviewPayload payload = new InterviewResponseDto.InterviewPayload(
                candidate.getCandidateId(),
                candidate.getUserEmail(),
                candidate.getEmailId(),
                candidate.getClientEmail()
        );

        // Return response DTO with success = true
        return new InterviewResponseDto(true,
                "Interview scheduled successfully.",
                payload,
                null);  // No errors
    }
    public List<GetInterviewResponseDto> getAllScheduledInterviews(String userId) {
        // Assume you fetch interview records from your repository
        List<CandidateDetails> candidates = candidateRepository.findByUserId(userId);

        // List to store mapped response DTOs
        List<GetInterviewResponseDto> response = new ArrayList<>();

        // Map each Interview entity to GetInterviewResponseDto
        for (CandidateDetails interview : candidates) {
            GetInterviewResponseDto dto = new GetInterviewResponseDto(
                    interview.getJobId(),
                    interview.getCandidateId(),
                    interview.getFullName(),
                    interview.getContactNumber(),
                    interview.getEmailId(),
                    interview.getUserEmail(),
                    interview.getUserId(),
                    interview.getInterviewDateTime(),
                    interview.getDuration(),
                    interview.getZoomLink(),
                    interview.getTimestamp(),
                    interview.getClientEmail(),
                    interview.getClientName(),
                    interview.getInterviewLevel()
            );
            response.add(dto);
        }

        return response;
    }

}


