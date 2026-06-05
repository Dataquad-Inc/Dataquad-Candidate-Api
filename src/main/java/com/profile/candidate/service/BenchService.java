package com.profile.candidate.service;

import com.profile.candidate.client.RequirementClient;
import com.profile.candidate.client.TimesheetClient;
import com.profile.candidate.client.UserClient;
import com.profile.candidate.dto.*;
import com.profile.candidate.exceptions.CandidateNotFoundException;
import com.profile.candidate.exceptions.DateRangeValidationException;
import com.profile.candidate.model.BenchDetails;
import com.profile.candidate.model.Submissions;
import com.profile.candidate.repository.BenchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.Collections;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BenchService {
    private final BenchRepository benchRepository;


    private static final Logger logger = LoggerFactory.getLogger(BenchService.class);

    @Autowired
    public BenchService(BenchRepository benchRepository) {
        this.benchRepository = benchRepository;
    }

    @Autowired
    private UserClient userClient;

    @Autowired
    private TimesheetClient timesheetClient;

    @Autowired
    private EmailService emailregisterService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RequirementClient requirementClient;

    private String generateNextUserId() {

        String sql = """
        SELECT CONCAT(
            'ADRTIN',
            LPAD(COALESCE(MAX(CAST(SUBSTRING(user_id, 7) AS UNSIGNED)), 0) + 1, 4, '0')
        ) AS next_user_id
        FROM user_details
        WHERE user_id LIKE 'ADRTIN%'
    """;

        return jdbcTemplate.queryForObject(sql, String.class);
    }


    public UserDetailsDTO createUserFromExistingBench(String benchId) {

        BenchDetails bench = benchRepository.findById(benchId)
                .orElseThrow(() -> {
                    logger.error("Bench candidate not found with ID: {}", benchId);
                    return new RuntimeException("Bench candidate not found with ID: " + benchId);
                });

        // Prevent duplicate registration
        if (Boolean.TRUE.equals(bench.getRegister())) {
            throw new RuntimeException("User already created for this bench candidate");
        }

        UserDetailsDTO userDto = new UserDetailsDTO();

        // Generate User ID
        userDto.setUserId(generateNextUserId());

        // Bench Candidate Details
        userDto.setUserName(bench.getFullName());
        userDto.setEmail(bench.getEmail());

        // Additional User Fields
        userDto.setPersonalemail(bench.getEmail());
        userDto.setPhoneNumber(bench.getContactNumber());

        // Default Values
        userDto.setDob("1990-01-01");
        userDto.setGender("male");

        // Joining Date
        userDto.setJoiningDate(LocalDate.now());

        // Other Details
        userDto.setDesignation("Candidate");
        userDto.setStatus("ACTIVE");

        userDto.setRoles(Collections.singleton("EXTERNALEMPLOYEE"));

        userDto.setEntity("IN");

        // Generate Random Password
        String randomPassword = PasswordGenerator.generateRandomPassword(8);

        userDto.setPassword(randomPassword);
        userDto.setConfirmPassword(randomPassword);

        try {

            logger.info("Attempting to register bench user: {}", userDto.getUserId());

            ResponseEntity<ApiResponse<UserDetailsDTO>> response = userClient.registerUser(userDto);

            ApiResponse<UserDetailsDTO> apiResponse = response.getBody();

            if (response.getStatusCode().is2xxSuccessful()
                    && apiResponse != null
                    && apiResponse.isSuccess()) {

                // Update Bench Register Status
                bench.setRegister(true);

                benchRepository.save(bench);

                logger.info("Bench updated with register=true for benchId: {}", benchId);

                // Initialize Leave
                EmployeeLeaveSummaryDto leaveInitDto =
                        new EmployeeLeaveSummaryDto();

                leaveInitDto.setUserId(userDto.getUserId());

                leaveInitDto.setEmployeeName(userDto.getUserName());

                leaveInitDto.setEmployeeType("BENCH");

                leaveInitDto.setJoiningDate(LocalDate.now());

                leaveInitDto.setUpdatedBy(bench.getFullName());

                logger.info("Calling timesheet microservice to initialize leave for userId: {}",
                        userDto.getUserId());

                ApiResponse<EmployeeLeaveSummaryDto> leaveResponse = timesheetClient.initializeLeave(leaveInitDto);

                if (leaveResponse == null || !leaveResponse.isSuccess()) {

                    String errorCode =
                            leaveResponse != null
                                    && leaveResponse.getError() != null
                                    ? leaveResponse.getError().getErrorCode()
                                    : "UNKNOWN_ERROR";

                    String errorMessage =
                            leaveResponse != null
                                    && leaveResponse.getError() != null
                                    ? leaveResponse.getError().getErrorMessage()
                                    : "Leave initialization failed";

                    logger.error("Leave initialization failed with error code: {}, message: {}",
                            errorCode, errorMessage);

                    throw new RuntimeException(
                            "Leave initialization failed with error code: "
                                    + errorCode
                                    + ", message: "
                                    + errorMessage);

                } else {

                    logger.info("Leave initialized successfully for userId: {}",
                            userDto.getUserId());
                }

                // Send Credentials Email
                emailregisterService.sendPasswordEmailHtml(
                        userDto.getEmail(),
                        userDto.getUserName(),
                        randomPassword
                );

                logger.info("Password email sent to: {}", userDto.getEmail());

                logger.info("Bench user registration succeeded for userId: {}",
                        userDto.getUserId());

            } else {

                String errorCode =
                        apiResponse != null
                                && apiResponse.getError() != null
                                ? apiResponse.getError().getErrorCode()
                                : "UNKNOWN_ERROR";

                String errorMessage =
                        apiResponse != null
                                && apiResponse.getError() != null
                                ? apiResponse.getError().getErrorMessage()
                                : "User registration failed";

                logger.error("User registration failed with error code: {}, message: {}",
                        errorCode, errorMessage);

                throw new RuntimeException(
                        "User creation failed with error code: "
                                + errorCode
                                + ", message: "
                                + errorMessage);
            }

        } catch (Exception e) {

            logger.error("Exception occurred during bench user creation", e);
            throw e;
        }

        return userDto;
    }

    public Map<String, Object> findAllBenchDetails(int page, int size,String search) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BenchDetails> benchPage;
        if (search != null && !search.trim().isEmpty()) {
            benchPage = benchRepository.searchBenchDetails(search, pageable);
        } else {
            benchPage = benchRepository.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", benchPage.getContent());
        response.put("currentPage", benchPage.getNumber());
        response.put("totalItems", benchPage.getTotalElements());
        response.put("totalPages", benchPage.getTotalPages());

        return response;
    }

    public String generateCustomId() {
        // Fetch all existing Bench IDs that follow the pattern "BENCH###"
        List<Integer> existingNumbers = benchRepository.findAll().stream()
                .map(BenchDetails::getId)
                .filter(id -> id != null && id.matches("BENCH\\d+"))  // Filter only valid "BENCH###" IDs
                .map(id -> Integer.parseInt(id.replace("BENCH", "")))  // Extract number
                .toList();

        // Find the highest existing number
        int nextNumber = existingNumbers.stream().max(Integer::compare).orElse(0) + 1;
        // ✅ Generate new ID in "BENCH001" format
        return String.format("BENCH%03d", nextNumber);
    }



   /* @Transactional
    public BenchDetails saveBenchDetails(BenchDetails benchDetails, MultipartFile resumeFile) throws IOException {
        // ✅ Check for duplicate email
        System.out.println("calling the BenchSave service method ");
        if (benchRepository.existsByEmail(benchDetails.getEmail())) {
            throw new IllegalArgumentException("Duplicate entry: Email already exists -> " + benchDetails.getEmail());
        }

        // ✅ Check for duplicate contact number
        if (benchRepository.existsByContactNumber(benchDetails.getContactNumber())) {
            throw new IllegalArgumentException("Duplicate entry: Contact number already exists -> " + benchDetails.getContactNumber());
        }

        // ✅ Check for duplicate full name
//        if (benchRepository.existsByFullName(benchDetails.getFullName())) {
//            throw new IllegalArgumentException("Duplicate entry: Full name already exists -> " + benchDetails.getFullName());
//        }

        // ✅ Auto-generate ID if not provided
        if (benchDetails.getId() == null || benchDetails.getId().isEmpty()) {
            benchDetails.setId(generateCustomId());
        }
        benchDetails.setCreatedDate(LocalDate.now());

        // ✅ Store resume if provided
        if (resumeFile != null && !resumeFile.isEmpty()) {
            benchDetails.setResume(resumeFile.getBytes());
        }

        if (benchDetails.getTechnology() != null) {
            benchDetails.setTechnology(benchDetails.getTechnology());
        }
        return benchRepository.save(benchDetails);
    }
 **/
   @Transactional
   public BenchDetails saveBenchDetails(
           BenchDetails benchDetails,
           MultipartFile resumeFile
   ) throws IOException {

       System.out.println("Calling Bench save service method");

       // ✅ Single DB call for duplicate check
       boolean exists = benchRepository
               .existsByEmailOrContactNumber(
                       benchDetails.getEmail(),
                       benchDetails.getContactNumber()
               );

       if (exists) {

           // More precise error message
           if (benchRepository.existsByEmail(
                   benchDetails.getEmail())) {

               throw new IllegalArgumentException(
                       "Duplicate entry: Email already exists -> "
                               + benchDetails.getEmail()
               );
           }

           if (benchRepository
                   .existsByContactNumber(
                           benchDetails.getContactNumber())) {

               throw new IllegalArgumentException(
                       "Duplicate entry: Contact number already exists -> "
                               + benchDetails.getContactNumber()
               );
           }
       }

       // ✅ Auto-generate ID (optimized)
       if (benchDetails.getId() == null
               || benchDetails.getId().isEmpty()) {

           Integer maxNumber =
                   benchRepository.findMaxBenchNumber();

           int nextNumber =
                   (maxNumber == null)
                           ? 1
                           : maxNumber + 1;

           String generatedId =
                   String.format(
                           "BENCH%03d",
                           nextNumber
                   );

           benchDetails.setId(
                   generatedId
           );
       }

       // ✅ Set created date
       benchDetails.setCreatedDate(
               LocalDate.now()
       );

       // ✅ Store resume only if provided
       if (resumeFile != null
               && !resumeFile.isEmpty()) {

           // Optional file size validation (5MB)
           long maxFileSize =
                   5 * 1024 * 1024;

           if (resumeFile.getSize()
                   > maxFileSize) {

               throw new IllegalArgumentException(
                       "Resume file size exceeds 5MB limit"
               );
           }

           benchDetails.setResume(
                   resumeFile.getBytes()
           );
       }

       // ✅ Set technology only if present
       if (benchDetails.getTechnology()
               != null) {

           benchDetails.setTechnology(
                   benchDetails.getTechnology()
           );
       }

       // ✅ Save
       return benchRepository.save(
               benchDetails
       );
   }

    @Transactional
    public BenchDetails updateBenchDetails(String id, BenchDetails benchDetails) {
        return benchRepository.findById(id).map(existingBench -> {
            // ✅ Check for duplicate fullName, email, or contactNumber (excluding the current ID)
//            if (benchDetails.getFullName() != null && benchRepository.existsByFullNameAndIdNot(benchDetails.getFullName(), id)) {
//                throw new IllegalArgumentException("Duplicate entry: Full Name already exists.");
//            }

            if (benchDetails.getEmail() != null && benchRepository.existsByEmailAndIdNot(benchDetails.getEmail(), id)) {
                throw new IllegalArgumentException("Duplicate entry: Email already exists.");
            }

            if (benchDetails.getContactNumber() != null && benchRepository.existsByContactNumberAndIdNot(benchDetails.getContactNumber(), id)) {
                throw new IllegalArgumentException("Duplicate entry: Contact Number already exists.");
            }

            // ✅ Update only non-null fields
            if (benchDetails.getFullName() != null) existingBench.setFullName(benchDetails.getFullName());
            if (benchDetails.getEmail() != null) existingBench.setEmail(benchDetails.getEmail());
            if (benchDetails.getRelevantExperience() != null) existingBench.setRelevantExperience(benchDetails.getRelevantExperience());
            if (benchDetails.getTotalExperience() != null) existingBench.setTotalExperience(benchDetails.getTotalExperience());
            if (benchDetails.getContactNumber() != null && !benchDetails.getContactNumber().isBlank()) {
                existingBench.setContactNumber(benchDetails.getContactNumber());
            }
            if (benchDetails.getSkills() != null) existingBench.setSkills(benchDetails.getSkills());
            if (benchDetails.getResume() != null && benchDetails.getResume().length > 0) {
                existingBench.setResume(benchDetails.getResume());  // Store the byte array of resume
            }
            if (benchDetails.getLinkedin() != null) existingBench.setLinkedin(benchDetails.getLinkedin());
            if (benchDetails.getReferredBy() != null) existingBench.setReferredBy(benchDetails.getReferredBy());
            if (benchDetails.getTechnology() != null) existingBench.setTechnology(benchDetails.getTechnology());
            if (benchDetails.getRemarks() != null) existingBench.setRemarks(benchDetails.getRemarks());
            if (benchDetails.getTags() != null) existingBench.setTags(benchDetails.getTags());
            return benchRepository.save(existingBench);
        }).orElseThrow(() -> new IllegalArgumentException("BenchDetails with ID " + id + " not found"));
    }


    @Transactional
    public void deleteBenchDetailsById(String id) {
        if (!benchRepository.existsById(id)) {
            throw new RuntimeException("Bench details with ID " + id + " not found.");
        }

        try {
            benchRepository.deleteByIdIgnoreCase(id);
            System.out.println("Successfully deleted BenchDetails with ID: " + id);
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting BenchDetails with ID: " + id + " -> " + e.getMessage());
        }
    }

    public List<BenchDetails> findBenchDetailsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            // ✅ Optional: Cap the date range to 31 days
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 31) {
                throw new DateRangeValidationException("Date range must not exceed one month.");
            }

            // ✅ Validate: Start date must be within the last 1 month
            LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
            // ✅ First validate basic date logic
            if (endDate.isBefore(startDate)) {
                throw new DateRangeValidationException("End date must not be before the start date.");
            }

            // ✅ Then validate start date is within the last month
            if (startDate.isBefore(oneMonthAgo)) {
                throw new DateRangeValidationException("Start date must be within the last 1 month.");
            }

            // ✅ Fetch bench details based on createdDate range
            List<BenchDetails> benchDetails = benchRepository.findByCreatedDateBetween(startDate, endDate);
            return benchDetails;
        } catch (DateRangeValidationException e) {
            logger.error("Date Range Validation Error: {}", e.getMessage());
            throw e;  // Re-throw the exception for higher-level handling
        } catch (Exception e) {
            logger.error("An error occurred while fetching bench details: ", e);
            throw new RuntimeException("Something went wrong while processing your request. Please try again later.");
        }
    }

    public List<String> getAllTags() {
        return benchRepository.getAllTags();
    }

    public List<Map<String, Object>> getTagCounts() {

        List<Object[]> results = benchRepository.getTagCounts();

        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("tag name", row[0]);
            map.put("count", row[1]);

            response.add(map);
        }

        return response;
    }

    public Map<String, Object> getBenchProfilesByTag(String tagName) {

        List<BenchDetailsDto> candidates =
                benchRepository.findBenchProfilesByTag(tagName);

        System.out.println("Total candidates found: " + candidates.size());

        Map<String, Object> response = new HashMap<>();

        response.put("tagName", tagName);
        response.put("count", candidates.size());
        response.put("candidates", candidates);

        return response;
    }

    public BenchDetailsDto getBenchById(String benchId) {
        Optional<BenchDetails> optionalBench = benchRepository.findById(benchId);
        if (optionalBench.isPresent()) {
            BenchDetails bench = optionalBench.get();

            // Map BenchDetails to BenchDetailsDto manually
            BenchDetailsDto dto = new BenchDetailsDto();
            dto.setId(bench.getId());
            dto.setFullName(bench.getFullName());
            dto.setEmail(bench.getEmail());
            dto.setRelevantExperience(bench.getRelevantExperience());
            dto.setTotalExperience(bench.getTotalExperience());
            dto.setContactNumber(bench.getContactNumber());
            dto.setSkills(bench.getSkills());
            dto.setLinkedin(bench.getLinkedin());
            dto.setReferredBy(bench.getReferredBy());
            dto.setCreatedDate(bench.getCreatedDate());
            dto.setTechnology(bench.getTechnology());
            dto.setRemarks(bench.getRemarks());
            dto.setTags(bench.getTags());

            return dto;
        } else {
            return null;
        }
    }

    @Transactional
    public String sendJdToBenchCandidates(String requirementId, List<String> benchIds) {
        try {
            System.out.println("Requirement ID = " + requirementId);
            System.out.println("Bench IDs = " + benchIds);

            // ==========================
            // FETCH REQUIREMENT
            // ==========================
            ResponseEntity<RequirementResponseDto> response = requirementClient.getRequirementById(requirementId);

            System.out.println("Feign Response Status = " + response.getStatusCode());
            System.out.println("Feign Response Body = " + response.getBody());

            RequirementResponseDto responseDto = response.getBody();

            if (responseDto == null || responseDto.getRequirement() == null) {
                throw new RuntimeException("Requirement not found");
            }
            RequirementResponseDto.Requirement requirement = responseDto.getRequirement();
            String jobTitle = requirement.getJobTitle();
            String jobDescription = requirement.getJobDescription();
            System.out.println("Job Title from DTO = " + jobTitle);
            System.out.println("Job Description from DTO = " + jobDescription);
            // fallback to avoid null mail subject/body
            if (jobTitle == null || jobTitle.trim().isEmpty()) {

                jobTitle = "Job Opportunity";
            }

            if (jobDescription == null || jobDescription.trim().isEmpty()) {

                jobDescription = "Job description not available.";
            }

            System.out.println("Final Job Title = " + jobTitle);
            System.out.println("Final Job Description = " + jobDescription);

            // ==========================
            // FETCH BENCH CANDIDATES
            // ==========================
            List<BenchDetails> candidates = benchRepository.findByIdIn(benchIds);

            System.out.println("Candidates found = " + candidates.size());

            if (candidates.isEmpty()) {
                throw new RuntimeException("No bench candidates found");
            }

            int successCount = 0;

            // ==========================
            // SEND MAILS
            // ==========================
            for (BenchDetails bench : candidates) {

                try {
                    System.out.println("Sending mail to: " + bench.getEmail());

                    emailregisterService.sendBenchJdMail(
                                    bench.getEmail(),
                                    bench.getFullName(),
                                    jobTitle,
                                    jobDescription
                            );

                    successCount++;

                    System.out.println("Mail sent successfully to: " + bench.getEmail());

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Mail failed for: " + bench.getEmail() + " Error: " + e.getMessage()
                    );
                }
            }

            return successCount + " JD mails sent successfully";

        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException("Failed to send JD mails: " + e.getMessage());
        }
    }
}