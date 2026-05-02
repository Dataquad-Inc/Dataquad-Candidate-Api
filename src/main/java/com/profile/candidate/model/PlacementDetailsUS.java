package com.profile.candidate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "placements_us")
@NoArgsConstructor
@AllArgsConstructor
public class PlacementDetailsUS {

    @Id
    @Column(name = "placement_id", updatable = false, nullable = false)
    private String id;

    @Column(name = "candidate_full_name")
    private String candidateFullName;

    @Pattern(regexp = "^\\d{10}$", message = "contactNumber must be 10 digits")
    @NotBlank(message = "contact number is required")
    @Column(name = "candidate_contact_no")
    private String candidateContactNo;

    @Column(name = "technology")
    private String technology;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "candidate_id")
    private String candidateId;

    @Column(name = "candidate_email_id")
    private String candidateEmailId;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "recruiter")
    private String recruiterName;

    @Column(name = "sales")
    private String sales;

    @Column(name = "bill_rate")
    private String billRate;

    @Column(name = "pay_rate")
    private String payRate;

    @Column(name = "gross_profit")
    private String grossProfit;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "status")
    private String status = "";

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "interview_id")
    private String interviewId;

    @Column(name = "is_register")
    private Boolean isRegister = false;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @DecimalMin(value = "0.0", inclusive = false, message = "Pay Rate must be a positive number")
    @Digits(integer = 10, fraction = 5, message = "Invalid format for Pay Rate")
    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "employee_working_type")
    private String employeeWorkingType = "MONTHLY";

    @Column(name = "user_id")
    private String userId;

    @Column(name = "hold_rate")
    private Integer holdRate;

    @Column(name = "referal")
    private String referal;

    @Column(name = "project_in")
    private String projectIn;

    @Column(name = "visa")
    private String visa;

    @Column(name = "project_in_c2c_sub_vendor_name")
    private String projectInC2cSubVendorName;

    @Column(name = "currency")
    private String currency;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }
}
