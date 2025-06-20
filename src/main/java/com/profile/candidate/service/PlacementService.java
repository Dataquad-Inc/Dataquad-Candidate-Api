package com.profile.candidate.service;

import com.profile.candidate.dto.EncryptionVerifyDto;
import com.profile.candidate.dto.PlacementDto;
import com.profile.candidate.dto.PlacementResponseDto;
import com.profile.candidate.exceptions.*;
import com.profile.candidate.model.InterviewDetails;
import com.profile.candidate.model.PlacementDetails;
import com.profile.candidate.repository.CandidateRepository;
import com.profile.candidate.repository.InterviewRepository;
import com.profile.candidate.repository.PlacementRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PlacementService {

    @Autowired
    PlacementRepository placementRepository;

    @PostConstruct
    public void init() {
        startOtpCleanupTask();
    }

    private static final Logger logger = LoggerFactory.getLogger(PlacementService.class);

    @Autowired
    private InterviewService interviewService;
    @Autowired
    private CandidateRepository candidateRepository;


    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Autowired
    private InterviewRepository interviewRepository;
    @Autowired
    private InterviewEmailService emailService;

    private String generateCustomId() {
        List<Integer> existingNumbers = placementRepository.findAll().stream()
                .map(PlacementDetails::getId)
                .filter(id -> id != null && id.matches("PLMNT\\d{4}"))
                .map(id -> Integer.parseInt(id.replace("PLMNT", "")))
                .toList();

        int nextNumber = existingNumbers.stream().max(Integer::compare).orElse(0) + 1;
        return String.format("PLMNT%04d", nextNumber);
    }


    @Transactional
    public PlacementResponseDto savePlacement(PlacementDto placementDto) {
        PlacementDetails placementDetails = convertToEntity(placementDto);

        // Check for existing placement by candidateContactNo and clientName
        PlacementDetails existingPlacement = placementRepository
                .findByCandidateContactNoAndClientName(placementDto.getCandidateContactNo(), placementDto.getClientName());

        if (existingPlacement != null) {
            throw new CandidateAlreadyExistsException("Placement already exists for this candidate and client.");
        }
        // Generate custom ID
        placementDetails.setId(generateCustomId());
        logger.info("Generated ID is: {}", placementDetails.getId());

        // Check interview details and status
        Optional<InterviewDetails> interviewDetailsOpt = interviewRepository
                .findByContactNumberAndCandidateEmailId(placementDto.getCandidateContactNo(), placementDto.getCandidateEmailId());

        if (interviewDetailsOpt.isPresent()) {
            InterviewDetails interviewDetails = interviewDetailsOpt.get();
            String latestStatus = interviewService.latestInterviewStatusFromJson(interviewDetails.getInterviewStatus());
            logger.info("Latest interview status: {}", latestStatus);

            // Ensure status is "placed"
            if (!"placed".equalsIgnoreCase(latestStatus)) {
                throw new CandidateNotFoundException("Candidate status is not placed in the interview table.");
            }

            // Check for duplicate interviewId
            if (placementDto.getInterviewId() != null) {
                boolean alreadyPlaced = placementRepository.existsByInterviewId(placementDto.getInterviewId());
                if (alreadyPlaced) {
                    throw new DuplicateInterviewPlacementException(
                            "Interview ID " + placementDto.getInterviewId() + " is already used in a placement.");
                }
            }

            // Mark candidate as placed in interview
            interviewDetails.setIsPlaced(true);
            interviewRepository.save(interviewDetails);
            logger.info("Interview details updated with isPlaced=true: {}", interviewDetails);
        } else {
            logger.warn("No matching interview details found. Proceeding without updating interview.");
        }

        // Set placement status and save
        placementDetails.setStatus("Active");
        PlacementDetails saved = placementRepository.save(placementDetails);

        boolean isPlaced = "Active".equalsIgnoreCase(saved.getStatus());

        return new PlacementResponseDto(
                saved.getId(),
                saved.getCandidateFullName(),
                saved.getCandidateContactNo(),
                isPlaced
        );
    }


    public PlacementResponseDto updatePlacement(String id, PlacementDto dto) {
        PlacementDetails existing = placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found with ID: " + id));

        if (dto.getCandidateEmailId() != null && !dto.getCandidateEmailId().equals(existing.getCandidateEmailId())) {
            existing.setCandidateEmailId(dto.getCandidateEmailId());
        }

        if (dto.getCandidateContactNo() != null && !dto.getCandidateContactNo().equals(existing.getCandidateContactNo())) {
            existing.setCandidateContactNo(dto.getCandidateContactNo());
        }

        Optional.ofNullable(dto.getCandidateFullName()).ifPresent(existing::setCandidateFullName);
        Optional.ofNullable(dto.getTechnology()).ifPresent(existing::setTechnology);
        Optional.ofNullable(dto.getClientName()).ifPresent(existing::setClientName);
        Optional.ofNullable(dto.getVendorName()).ifPresent(existing::setVendorName);
        Optional.ofNullable(dto.getStartDate()).ifPresent(start ->
                existing.setStartDate(LocalDate.parse(start, formatter)));
        Optional.ofNullable(dto.getEndDate()).ifPresent(end ->
                existing.setEndDate(LocalDate.parse(end, formatter)));
        Optional.ofNullable(dto.getRecruiterName()).ifPresent(existing::setRecruiterName);
        Optional.ofNullable(dto.getSales()).ifPresent(existing::setSales);
        Optional.ofNullable(dto.getEmploymentType()).ifPresent(existing::setEmploymentType);
        Optional.ofNullable(dto.getRemarks()).ifPresent(existing::setRemarks);
        Optional.ofNullable(dto.getStatus()).ifPresent(existing::setStatus);
        Optional.ofNullable(dto.getStatusMessage()).ifPresent(existing::setStatusMessage);
        Optional.ofNullable(dto.getGrossProfit()).ifPresent(existing::setGrossProfit);
        if (dto.getPayRate() != null) {
            existing.setPayRate(dto.getPayRate());
        }

        if (dto.getBillRate() != null) {
            existing.setBillRate(dto.getBillRate());
        }

        PlacementDetails updated = placementRepository.save(existing);
        return convertToResponseDto(updated);

    }

    public void deletePlacement(String id) {
        if (!placementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Placement not found with ID: " + id);
        }
        placementRepository.deleteById(id);
    }

    public PlacementResponseDto getPlacementById(String id) {
        PlacementDetails placement = placementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Placement not found with ID: " + id));

        LocalDate now = LocalDate.now();
        if ("active".equalsIgnoreCase(placement.getStatus()) &&
                placement.getEndDate() != null &&
                now.isAfter(placement.getEndDate())) {

            placement.setStatus("completed");
            placementRepository.save(placement);
        }

        return convertToResponseDto(placement);
    }

    public List<PlacementDetails> getAllPlacements() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1); // 1st of current month
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth()); // last day of current month

        logger.info("Fetching placements between {} and {}", startDate, endDate);

        List<PlacementDetails> allPlacements = placementRepository.findPlacementsByCreatedAtBetween(startDate, endDate);
        logger.info("Total placements found: {}", allPlacements.size());

        List<PlacementDetails> updatedPlacements = new ArrayList<>();

        for (PlacementDetails placement : allPlacements) {
            // Update status if endDate has passed and status is still active
            if ("active".equalsIgnoreCase(placement.getStatus()) &&
                    placement.getEndDate() != null &&
                    now.isAfter(placement.getEndDate())) {

                placement.setStatus("completed");
                placementRepository.save(placement); // Save the change
            }

            // Only return non-inactive placements
            if (!"inactive".equalsIgnoreCase(placement.getStatus())) {
                updatedPlacements.add(placement);
            }
        }

        logger.info("Filtered placements count: {}", updatedPlacements.size());
        return updatedPlacements;
    }

    private PlacementResponseDto convertToResponseDto(PlacementDetails updated) {
        return new PlacementResponseDto(
                updated.getId(),
                updated.getCandidateFullName(),
                updated.getCandidateContactNo()

        );
    }

    private PlacementDetails convertToEntity(PlacementDto dto) {
        PlacementDetails entity = new PlacementDetails();
        entity.setCandidateFullName(dto.getCandidateFullName());
        entity.setCandidateContactNo(dto.getCandidateContactNo());
        entity.setCandidateEmailId(dto.getCandidateEmailId());
        entity.setCandidateId(dto.getCandidateId());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setTechnology(dto.getTechnology());
        entity.setClientName(dto.getClientName());
        entity.setVendorName(dto.getVendorName());
        entity.setStartDate(dto.getStartDate() != null ? LocalDate.parse(dto.getStartDate(), formatter) : null);
        entity.setEndDate(dto.getEndDate() != null ? LocalDate.parse(dto.getEndDate(), formatter) : null);
        entity.setRecruiterName(dto.getRecruiterName());
        entity.setSales(dto.getSales());
        entity.setPayRate(dto.getPayRate());
        entity.setBillRate(dto.getBillRate());
        entity.setEmploymentType(dto.getEmploymentType());
        entity.setRemarks(dto.getRemarks());
        entity.setStatus(dto.getStatus());
        entity.setStatusMessage(dto.getStatusMessage());
        entity.setInterviewId(dto.getInterviewId());
        entity.setGrossProfit(dto.getGrossProfit());
        return entity;
    }

    public Map<String, Long> getCounts(String recruiterId) {
        YearMonth currentMonth = YearMonth.now(); // e.g., 2025-05
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay(); // 2025-05-01T00:00
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59, 999_999_999); // 2025-05-31T23:59:59.999999999

        Object[] result = (Object[]) placementRepository.getAllCountsByDateRange(startOfMonth, endOfMonth, recruiterId);

        Map<String, Long> counts = new LinkedHashMap<>();

        // Index mapping based on the query
        counts.put("requirements", ((Number) result[0]).longValue());
        counts.put("candidates", ((Number) result[1]).longValue());
        counts.put("clients", ((Number) result[2]).longValue());

        counts.put("bench", ((Number) result[5]).longValue());
        counts.put("users", ((Number) result[6]).longValue());
        counts.put("interviews", ((Number) result[7]).longValue());
        counts.put("internalInterviews", ((Number) result[8]).longValue());
        counts.put("externalInterviews", ((Number) result[9]).longValue());
        counts.put("assigned", ((Number) result[10]).longValue());

        return counts;
    }


    public Map<String, Long> getCountsByDateRange(LocalDate fromDate, LocalDate toDate, String recruiterId) {
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

        Object[] result = (Object[]) placementRepository.getAllCountsByDateRange(startDateTime, endDateTime, recruiterId);

        Map<String, Long> counts = new LinkedHashMap<>(); // preserves insertion order

        // Correct index mapping based on the SQL query (11 values: index 0 to 10)
        counts.put("requirements", ((Number) result[0]).longValue());
        counts.put("candidates", ((Number) result[1]).longValue());
        counts.put("clients", ((Number) result[2]).longValue());
        counts.put("bench", ((Number) result[5]).longValue());
        counts.put("users", ((Number) result[6]).longValue());
        counts.put("interviews", ((Number) result[7]).longValue());
        counts.put("internalInterviews", ((Number) result[8]).longValue());
        counts.put("externalInterviews", ((Number) result[9]).longValue());
        counts.put("assigned", ((Number) result[10]).longValue());

        return counts;
    }


    public List<PlacementDetails> getPlacementsByDateRange(LocalDate startDate, LocalDate endDate) {
        return placementRepository.findPlacementsByCreatedAtBetween(startDate, endDate);
    }

    public Map<String, Long> getCountsForAll() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
        LocalDateTime endDateTime = lastDayOfMonth.atTime(LocalTime.MAX);

        // Pass an empty or default recruiter ID, or null if your repository handles that
        Object[] result = (Object[]) placementRepository.getAllCountsByDateRange(startDateTime, endDateTime, "");

        Map<String, Long> counts = new LinkedHashMap<>();

        counts.put("users", ((Number) result[6]).longValue());
        counts.put("clients", ((Number) result[2]).longValue());
        counts.put("requirements", ((Number) result[0]).longValue());
        counts.put("assigned", ((Number) result[10]).longValue());
        counts.put("candidates", ((Number) result[1]).longValue());
        counts.put("bench", ((Number) result[5]).longValue());
        counts.put("interviews", ((Number) result[7]).longValue());

        // Fix these two lines - they should match what works in the getCounts method
        counts.put("externalInterviews", ((Number) result[9]).longValue());  // Correct mapping for external
        counts.put("internalInterviews", ((Number) result[8]).longValue());  // Correct mapping for internal

        Object[] placementsWithOutDate = (Object[]) placementRepository.getPlacementCountsWithOutDate();
        counts.put("contractPlacements", ((Number) placementsWithOutDate[0]).longValue());
        counts.put("fulltimePlacements", ((Number) placementsWithOutDate[1]).longValue());



        return counts;
    }

    public Map<String, Long> getCountsByDateRangeForAll(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

        // Pass empty recruiterId to indicate no filter
        Object[] result = (Object[]) placementRepository.getAllCountsByDateRange(startDateTime, endDateTime, "");

        Map<String, Long> counts = new LinkedHashMap<>();

        counts.put("users", ((Number) result[6]).longValue());
        counts.put("clients", ((Number) result[2]).longValue());
        counts.put("requirements", ((Number) result[0]).longValue());
        counts.put("assigned", ((Number) result[10]).longValue());
        counts.put("candidates", ((Number) result[1]).longValue());
        counts.put("bench", ((Number) result[5]).longValue());
        counts.put("interviews", ((Number) result[7]).longValue());

        // Fix these two lines - they should match what works in the getCounts method
        counts.put("externalInterviews", ((Number) result[9]).longValue());  // Correct mapping for external
        counts.put("internalInterviews", ((Number) result[8]).longValue());  // Correct mapping for internal

        counts.put("contractPlacements", ((Number) result[3]).longValue());
        counts.put("fulltimePlacements", ((Number) result[4]).longValue());



        return counts;
    }

    private final Map<String, String> otpStorageOnUserId = new ConcurrentHashMap<>();
    private final Map<String, String> otpStorageOnPlacementId = new ConcurrentHashMap<>();
    private final Map<String, Long> otpTimestamps = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final long OTP_EXPIRY_TIME_MS = 5 * 60 * 1000; // 5 minutes
    private static final long OTP_COOLDOWN_MS = 60 * 1000; // 1 minute
    //String ADMIN_EMAIL_ID="putluruarunkumarreddy13@gmail.com";
    //String ADMIN_EMAIL_ID=placementRepository.findPrimarySuperAdminEmail();
    private void startOtpCleanupTask() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            logger.debug("Running OTP cleanup task at time: {}", currentTime);

            // Cleanup for Placement ID OTPs
            otpStorageOnPlacementId.entrySet().removeIf(entry -> {
                Long timestamp = otpTimestamps.get(entry.getKey());
                boolean expired = timestamp == null || (currentTime - timestamp) > OTP_EXPIRY_TIME_MS;
                if (expired) {
                    logger.info("Removing expired OTP for Placement ID: {}", entry.getKey());
                    otpTimestamps.remove(entry.getKey());
                }
                return expired;
            });
            // Cleanup for User ID OTPs
            otpStorageOnUserId.entrySet().removeIf(entry -> {
                Long timestamp = otpTimestamps.get(entry.getKey());
                boolean expired = timestamp == null || (currentTime - timestamp) > OTP_EXPIRY_TIME_MS;
                if (expired) {
                    logger.info("Removing expired OTP for User ID: {}", entry.getKey());
                    otpTimestamps.remove(entry.getKey());
                }
                return expired;
            });

            logger.debug("Current OTP storage after cleanup - Placement: {}", otpStorageOnPlacementId);
            logger.debug("Current OTP storage after cleanup - User: {}", otpStorageOnUserId);
        }, 1, 5, TimeUnit.MINUTES); // Initial delay: 1 min, Repeat every 5 mins
    }

    public String generateOtp(String userId, String placementId,boolean isNewPlacement) {

        List<String> ADMIN_EMAILS=placementRepository.findPrimarySuperAdminEmail();
        if (userId == null || placementId == null)
            throw new ResourceNotFoundException("User ID or Placement ID can not be null");

        String userName = candidateRepository.findUserNameByUserId(userId);
        if (userName == null) throw new UserNotFoundException("No User Found with ID " + userId);
        Optional<PlacementDetails> placementDetails = placementRepository.findById(placementId);
        if (placementDetails.isEmpty())
            throw new PlacementsNotFoundException("Placement Not Found With ID :" + placementId);

        String otp = String.format("%06d", random.nextInt(999999));
        LocalDateTime requestTime=LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");
        String formattedDateTime = requestTime.format(dateTimeFormatter);
        String subject="Authorization Required: OTP for Accessing Sensitive Placement Details";
        logger.info("send Email Otp Getting called.");

        long currentTime = System.currentTimeMillis();
        logger.info("Current Time In milli seconds {}", currentTime);
        otpTimestamps.put(userId, currentTime);
        if (otpTimestamps.containsKey(placementId) && (currentTime - otpTimestamps.get(placementId)) < OTP_COOLDOWN_MS) {
            throw new InvalidOTPException("Please wait before requesting a new OTP.");
        }
        emailService.sendOtpEmail(ADMIN_EMAILS, subject, emailBodyForViewAllPlacementsDetails(userName,otp,formattedDateTime,placementDetails.get().getCandidateFullName(),isNewPlacement));
        otpStorageOnUserId.put(userId, otp.trim());
        otpStorageOnPlacementId.put(placementId, otp.trim());
        otpTimestamps.put(placementId, currentTime);
        logger.info("OTP TimeStamp :" + otpTimestamps);
        logger.info("Stored OTP for {}: {}", userId, otp);
        logger.info("Stored OTP for {}: {}", placementId, otp);

        return "Otp Sent Successfully!";
    }
    public String emailBodyForViewAllPlacementsDetails(String userName, String otp, String requestTime, String candidateName,boolean isNewPlacement) {

        String safeUserName = (userName != null) ? userName : "Unknown User";
        String safeRequestTime = (requestTime != null) ? requestTime : "N/A";
        String safeOtp = (otp != null) ? otp : "N/A";
        String safeCandidateName = (candidateName != null) ? candidateName : null;
        if(candidateName==null) {
            safeCandidateName = isNewPlacement ? "New Candidate" : null;
        }
        String action = safeCandidateName != null ? "UPDATE" : "VIEW";
        if (isNewPlacement){
            action =  "UPDATE";
        }
        String actionColor = "#3366ff";

        String candidateLine = safeCandidateName != null ?
                String.format("<li><b>Candidate Name:</b> %s</li>", safeCandidateName) : "";

        String emailTemplate = "<p>Dear Admin,</p>" +
                "<p>A user has requested to <span style='color: %s; font-weight: bold;'>%s</span> sensitive placement information and requires authorization.</p>" +
                "<p><b>Request Details:</b></p>" +
                "<ul>" +
                "<li><b>User:</b> %s</li>" +
                "%s" +
                "<li><b>Request Time:</b> %s</li>" +
                "</ul>" +
                "<p>Please use the following OTP to authorize this request</p>" +
                "<p>One Time Password (OTP): <span style='color: #000000; font-size: 1.2em; font-weight: bold;'>%s</span></p>" +
                "<p><b>Note:</b> This OTP is valid for 5 minutes only. Do not share it with anyone.</p>" +
                "<p>If you didn't initiate this request, please contact your system administrator immediately.</p>";

        return String.format(emailTemplate,
                actionColor,
                action,
                safeUserName,
                candidateLine,
                safeRequestTime,
                safeOtp
        );
    }
    public String generateOtp(String userId,boolean isNewPlacement) {

        logger.info("Email SMS started..");
        List<String> ADMIN_EMAILS=placementRepository.findPrimarySuperAdminEmail();
        if (userId == null) throw new ResourceNotFoundException("User ID or Placement ID can not be null");
        String userName = candidateRepository.findUserNameByUserId(userId);
        if (userName == null) throw new UserNotFoundException("No User Found with ID " + userId);
        String otp = String.format("%06d", random.nextInt(999999));
        LocalDateTime requestTime=LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a");
        String formattedDateTime = requestTime.format(dateTimeFormatter);
        String subject="Authorization Required: OTP for Accessing Sensitive Placement Details";

        long currentTime = System.currentTimeMillis();
        logger.info("Current Time In milli seconds {}", currentTime);

        if (otpTimestamps.containsKey(userId) && (currentTime - otpTimestamps.get(userId)) < OTP_COOLDOWN_MS) {
            throw new InvalidOTPException("Please wait before requesting a new OTP.");
        }

        emailService.sendOtpEmail(ADMIN_EMAILS, subject, emailBodyForViewAllPlacementsDetails(userName,otp,formattedDateTime,null,isNewPlacement));
        otpStorageOnUserId.put(userId, otp.trim());
        otpStorageOnPlacementId.put(userId, otp.trim());
        otpTimestamps.put(userId, currentTime);
        logger.info("OTP TimeStamp :" + otpTimestamps);
        logger.info("Stored OTP for {}: {}", userId, otp);

        return "Otp Sent Successfully!";
    }

    public String verifyOtp(EncryptionVerifyDto encryptDTO) {
        logger.info("OTP Verification Started ");
        if (encryptDTO.getUserId() == null) {
            throw new ResourceNotFoundException("UserID Or PlacementID can not be null");
        }
        String userName = candidateRepository.findUserNameByUserId(encryptDTO.getUserId());
        if (userName == null) throw new UserNotFoundException("No User Found with ID " + encryptDTO.getUserId());

        if (encryptDTO.getPlacementId()!=null){
            Optional<PlacementDetails> placementDetails = placementRepository.findById(encryptDTO.getPlacementId());
            if (placementDetails.isEmpty()) {
                logger.error("Placement Not Found with ID {}", encryptDTO.getPlacementId());
                throw new PlacementsNotFoundException("Placement Not Found With ID :" + encryptDTO.getPlacementId());
            }

            String placementId = encryptDTO.getPlacementId().trim();
            String enteredOtp = encryptDTO.getOtp().trim();
            // Retrieve stored OTP
            String storedOtp = otpStorageOnPlacementId.get(placementId);
            Long timestamp = otpTimestamps.get(placementId);
            logger.info("Verifying OTP for {}. Entered: {}, Stored: {}", placementId, enteredOtp, storedOtp);
            logger.info("Current OTP storage before verification: {}", otpStorageOnPlacementId);
            logger.info("Current OTP with TimeStamp :{}", timestamp);
            if (storedOtp == null) {
                logger.warn("No OTP found for placementId: {}", placementId);
                throw new InvalidOTPException("OTP can not be null");
            }
            long currentTime = System.currentTimeMillis();
            if ((currentTime - timestamp) > OTP_EXPIRY_TIME_MS) {
                otpStorageOnPlacementId.remove(placementId); // Remove expired OTP
                otpTimestamps.remove(placementId); // Remove expired timestamp
                otpStorageOnUserId.remove(encryptDTO.getUserId());
                logger.warn("OTP expired for placementId: {}", placementId);
                throw new InvalidOTPException("OTP has expired for " + placementId);
            }
            if (storedOtp.equals(enteredOtp)) {
                otpStorageOnPlacementId.remove(placementId); // Remove OTP after successful verification
                otpTimestamps.remove(placementId); // Remove timestamp to allow new requests
                logger.info("OTP verification successful for {}", placementId);
                return "Verification successful! " + placementId;
            } else {
                logger.warn("OTP verification failed for {}. Entered: {}, Expected: {}", placementId, enteredOtp, storedOtp);
                throw new InvalidOTPException("Wrong OTP, Please Enter Valid OTP ");
            }

        }else{
            String userId=encryptDTO.getUserId();
            String enteredOtp = encryptDTO.getOtp().trim();
            // Retrieve stored OTP
            String storedOtp = otpStorageOnPlacementId.get(userId);
            Long timestamp = otpTimestamps.get(userId);
            logger.info("Verifying OTP for {}. Entered: {}, Stored: {}", userId, enteredOtp, storedOtp);
            logger.info("Current OTP storage before verification: {}", otpStorageOnPlacementId);
            logger.info("Current OTP with TimeStamp :{}", timestamp);
            if (storedOtp == null) {
                logger.warn("No OTP found for userID: {}", userId);
                throw new InvalidOTPException("OTP can not be null");
            }
            long currentTime = System.currentTimeMillis();
            if ((currentTime - timestamp) > OTP_EXPIRY_TIME_MS) {
                otpStorageOnUserId.remove(userId); // Remove expired OTP
                otpTimestamps.remove(userId); // Remove expired timestamp
                logger.warn("OTP expired for placementId: {}", userId);
                throw new InvalidOTPException("OTP has expired for " + userId);
            }
            if (storedOtp.equals(enteredOtp)) {
                otpStorageOnPlacementId.remove(userId); // Remove OTP after successful verification
                otpTimestamps.remove(userId); // Remove timestamp to allow new requests
                logger.info("OTP verification successful for {}", userId);
                return "Verification successful! " + userId;
            } else {
                logger.warn("OTP verification failed for {}. Entered: {}, Expected: {}", userId, enteredOtp, storedOtp);
                throw new InvalidOTPException("Wrong OTP, Please Enter Valid OTP ");
            }

        }


    }

}