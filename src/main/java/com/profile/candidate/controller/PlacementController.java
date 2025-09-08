package com.profile.candidate.controller;

import com.profile.candidate.dto.*;
import com.profile.candidate.exceptions.ResourceNotFoundException;
import com.profile.candidate.model.PlacementDetails;
import com.profile.candidate.service.PlacementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = {
        "http://35.188.150.92", "http://192.168.0.140:3000", "http://192.168.0.139:3000",
        "https://mymulya.com", "http://localhost:3000", "http://192.168.0.135:8080",
        "http://192.168.0.135:80", "http://localhost/", "http://mymulya.com:443",
        "http://182.18.177.16:443", "http://localhost/","http://192.168.1.141:3000"
})
@RestController
@RequestMapping("/candidate")
public class PlacementController {

    @Autowired
    private PlacementService placementService;
    private static final Logger logger = LoggerFactory.getLogger(PlacementController.class);


    // Save placement
    @PostMapping("/placement/create-placement")
    public ResponseEntity<?> savePlacement(@Valid @RequestBody PlacementDto placementDto) {
        PlacementResponseDto savedPlacement = placementService.savePlacement(placementDto);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Placement saved successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", savedPlacement);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update placement by ID
    @PutMapping("/placement/update-placement/{id}")
    public ResponseEntity<?> updatePlacement(@PathVariable String id, @Valid @RequestBody PlacementDto placementDto) {
        try {
            PlacementResponseDto updated = placementService.updatePlacement(id, placementDto);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Placement updated successfully");
            response.put("timestamp", LocalDateTime.now());
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    // Delete placement by ID
    @DeleteMapping("/placement/delete-placement/{id}")
    public ResponseEntity<?> deletePlacement(@PathVariable String id) {
        try {
            placementService.deletePlacement(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Placement with ID " + id + " deleted successfully",
                    "timestamp", LocalDateTime.now()
            ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/placement/placements-list")
    public ResponseEntity<?> getAllPlacements(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String email) {

        LocalDate start;
        LocalDate end;
        LocalDate now = LocalDate.now();

        // If startDate/endDate not provided, default to current month's range
        if (startDate != null && endDate != null) {
            start = LocalDate.parse(startDate); // Expecting yyyy-MM-dd
            end = LocalDate.parse(endDate);
        } else {
            start = now.withDayOfMonth(1);
            end = now.withDayOfMonth(now.lengthOfMonth());
        }

        List<PlacementDetails> placements;

        if (email != null && !email.isEmpty()) {
            placements = placementService.getPlacementsByCandidateEmailAndDateRange(email, start, end);
        } else {
            placements = placementService.getAllPlacements(start, end);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Placements fetched successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", placements);

        return ResponseEntity.ok(response);
    }


    // Get placement by ID
    @GetMapping("/placement/{id}")
    public ResponseEntity<?> getPlacementById(@PathVariable String id) {
        try {
            PlacementResponseDto placement = placementService.getPlacementById(id);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Placement fetched successfully");
            response.put("timestamp", LocalDateTime.now());
            response.put("data", placement);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
    @GetMapping("/dashboardcounts")
    public ResponseEntity<?> getDashboardCounts(@RequestParam(required = false) String recruiterId) {
        Map<String, Long> counts;

        // If recruiterId is provided, use it, otherwise fetch counts without filtering by recruiter
        if (recruiterId != null && !recruiterId.trim().isEmpty()) {
            counts = placementService.getCounts(recruiterId);
        } else {
            counts = placementService.getCountsForAll(); // Fetch counts without recruiter-specific filter
        }

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/placement/filterByDate")
    public ResponseEntity<ApiResponse<List<PlacementDetails>>> getPlacementsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "email", required = false) String email
    ) {
        try {
            List<PlacementDetails> placements;
            if (email != null && !email.isEmpty()) {
                placements = placementService.getPlacementsByCandidateEmailAndDateRange(email, startDate, endDate);
            } else {
                placements = placementService.getPlacementsByDateRangeWithLoginStatus(startDate, endDate);
            }
            return ResponseEntity.ok(ApiResponse.success("Placements fetched successfully", placements));
        } catch (Exception e) {
            // Log the error with details
            logger.error("Error fetching placements", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch placements due to server error", "500", "Internal server error"));
        }
    }


    @GetMapping("/dashboardcounts/filterByDate")
    public ResponseEntity<?> getDashboardCountsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String recruiterId) {

        if (endDate.isBefore(startDate)) {
            logger.warn("End date {} is before start date {}", endDate, startDate);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "End date cannot be before start date"));
        }

        try {
            Map<String, Long> counts;

            if (recruiterId != null && !recruiterId.trim().isEmpty()) {
                counts = placementService.getCountsByDateRange(startDate, endDate, recruiterId);
            } else {
                counts = placementService.getCountsByDateRangeForAll(startDate, endDate); // Add this method to your service
            }

            if (counts.values().stream().allMatch(count -> count == 0)) {
                logger.warn("No dashboard data found between {} and {}", startDate, endDate);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No data found between " + startDate + " and " + endDate));
            }

            return ResponseEntity.ok(counts);

        } catch (Exception e) {
            logger.error("Error fetching dashboard counts between {} and {}", startDate, endDate, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while fetching dashboard counts"));
        }
    }

    @PostMapping("/generateOtp")
    public ResponseEntity<String> sendOtp(@RequestBody EncryptionRequestDTO dto) {

        logger.info("IsNewPlacement :"+dto.isNewPlacement());
        if(dto.getPlacementId()==null){
            String response = placementService.generateOtp(dto.getUserId(),dto.isNewPlacement());
            return ResponseEntity.ok(response);
        }else if(dto.isNewPlacement()){
            String response=placementService.generateOtp(dto.getUserId(),dto.isNewPlacement());
            return ResponseEntity.ok(response);
        }
        else {
            String response = placementService.generateOtp(dto.getUserId(), dto.getPlacementId(), dto.isNewPlacement());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<String> verifyOtp(@RequestBody EncryptionVerifyDto encryptDTO) {

        String response = placementService.verifyOtp(encryptDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{placementId}/create-user")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUserFromPlacement(@PathVariable String placementId) {
        try {
            UserDetailsDTO createdUser = placementService.createUserFromExistingPlacement(placementId);

            // Map UserDetailsDTO to UserResponseDTO
            UserResponseDTO responseDto = new UserResponseDTO();
            responseDto.setUserId(createdUser.getUserId());
            responseDto.setUserName(createdUser.getUserName());
            responseDto.setPassword(createdUser.getPassword());
            responseDto.setConfirmPassword(createdUser.getConfirmPassword());
            responseDto.setEmail(createdUser.getEmail());
            responseDto.setRoles(createdUser.getRoles());
            responseDto.setStatus(createdUser.getStatus());
            responseDto.setEntity(createdUser.getEntity());

            ApiResponse<UserResponseDTO> successResponse = ApiResponse.success("User created from placement successfully.", responseDto);
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            ApiResponse<UserResponseDTO> errorResponse = ApiResponse.error(
                    "User creation failed",
                    "USER_CREATION_ERROR",
                    e.getMessage()
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/allVendors")
    public ResponseEntity<ApiResponse<List<String>>> getAllVendorNames() {
        try {
            List<String> vendorNames = placementService.getAllVendorNames();
            return ResponseEntity.ok(ApiResponse.success("All vendor names fetched successfully", vendorNames));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch vendor names", "FETCH_ERROR", e.getMessage()));
        }
    }

}
