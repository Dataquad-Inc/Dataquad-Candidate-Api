package com.profile.candidate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PlacementDocs {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long documentId;

  private String fileName;

  @Lob
  @Column(name = "file_data", columnDefinition = "LONGBLOB")
  private byte[] fileData;

  private String documentType;

  private String fileType;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "placement_details_id")
  private PlacementDetails placementDetails;

  private LocalDateTime createdAt;
  private String deletedBy;
  private boolean isDeleted;
  private LocalDateTime deletedAt;


  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public long getDocumentId() {
    return documentId;
  }

  public void setDocumentId(long documentId) {
    this.documentId = documentId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public byte[] getFileData() {
    return fileData;
  }

  public void setFileData(byte[] fileData) {
    this.fileData = fileData;
  }

  public String getDocumentType() {
    return documentType;
  }

  public void setDocumentType(String documentType) {
    this.documentType = documentType;
  }

  public PlacementDetails getPlacementDetails() {
    return placementDetails;
  }

  public void setPlacementDetails(PlacementDetails placementDetails) {
    this.placementDetails = placementDetails;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean deleted) {
    isDeleted = deleted;
  }

  public String getDeletedBy() {
    return deletedBy;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public void setDeletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
  }

  public boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

}
