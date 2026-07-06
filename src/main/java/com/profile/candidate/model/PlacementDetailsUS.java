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
    private String holdRate;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateEmailId() {
        return candidateEmailId;
    }

    public void setCandidateEmailId(String candidateEmailId) {
        this.candidateEmailId = candidateEmailId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getRecruiterName() {
        return recruiterName;
    }

    public void setRecruiterName(String recruiterName) {
        this.recruiterName = recruiterName;
    }

    public String getSales() {
        return sales;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }

    public String getBillRate() {
        return billRate;
    }

    public void setBillRate(String billRate) {
        this.billRate = billRate;
    }

    public String getPayRate() {
        return payRate;
    }

    public void setPayRate(String payRate) {
        this.payRate = payRate;
    }

    public String getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(String grossProfit) {
        this.grossProfit = grossProfit;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public Boolean getRegister() {
        return isRegister;
    }

    public void setRegister(Boolean register) {
        isRegister = register;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getEmployeeWorkingType() {
        return employeeWorkingType;
    }

    public void setEmployeeWorkingType(String employeeWorkingType) {
        this.employeeWorkingType = employeeWorkingType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHoldRate() {
        return holdRate;
    }

    public void setHoldRate(String holdRate) {
        this.holdRate = holdRate;
    }

    public String getReferal() {
        return referal;
    }

    public void setReferal(String referal) {
        this.referal = referal;
    }

    public String getProjectIn() {
        return projectIn;
    }

    public void setProjectIn(String projectIn) {
        this.projectIn = projectIn;
    }

    public String getVisa() {
        return visa;
    }

    public void setVisa(String visa) {
        this.visa = visa;
    }

    public String getProjectInC2cSubVendorName() {
        return projectInC2cSubVendorName;
    }

    public void setProjectInC2cSubVendorName(String projectInC2cSubVendorName) {
        this.projectInC2cSubVendorName = projectInC2cSubVendorName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }
}
