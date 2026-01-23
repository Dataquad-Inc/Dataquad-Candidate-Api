package com.profile.candidate.dto;


import java.util.List;

public class TeamleadSubmissionsDTO {
    private List<SubmissionGetResponseDto> selfSubmissions;
    private List<SubmissionGetResponseDto> teamSubmissions;
    private long totalSelfSubmissions;
    private long totalTeamSubmissions;
    private int totalSelfPages;
    private int totalTeamPages;
    private int currentPage;
    private int pageSize;

    // Constructor accepting two lists (backward compatibility)
    public TeamleadSubmissionsDTO(List<SubmissionGetResponseDto> selfSubmissions, List<SubmissionGetResponseDto> teamSubmissions) {
        this.selfSubmissions = selfSubmissions;
        this.teamSubmissions = teamSubmissions;
    }

    // Constructor with pagination metadata
    public TeamleadSubmissionsDTO(List<SubmissionGetResponseDto> selfSubmissions, List<SubmissionGetResponseDto> teamSubmissions,
                                  long totalSelfSubmissions, long totalTeamSubmissions,
                                  int totalSelfPages, int totalTeamPages, int currentPage, int pageSize) {
        this.selfSubmissions = selfSubmissions;
        this.teamSubmissions = teamSubmissions;
        this.totalSelfSubmissions = totalSelfSubmissions;
        this.totalTeamSubmissions = totalTeamSubmissions;
        this.totalSelfPages = totalSelfPages;
        this.totalTeamPages = totalTeamPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    // Getters and setters
    public List<SubmissionGetResponseDto> getSelfSubmissions() {
        return selfSubmissions;
    }

    public void setSelfSubmissions(List<SubmissionGetResponseDto> selfSubmissions) {
        this.selfSubmissions = selfSubmissions;
    }

    public List<SubmissionGetResponseDto> getTeamSubmissions() {
        return teamSubmissions;
    }

    public void setTeamSubmissions(List<SubmissionGetResponseDto> teamSubmissions) {
        this.teamSubmissions = teamSubmissions;
    }

    public long getTotalSelfSubmissions() {
        return totalSelfSubmissions;
    }

    public void setTotalSelfSubmissions(long totalSelfSubmissions) {
        this.totalSelfSubmissions = totalSelfSubmissions;
    }

    public long getTotalTeamSubmissions() {
        return totalTeamSubmissions;
    }

    public void setTotalTeamSubmissions(long totalTeamSubmissions) {
        this.totalTeamSubmissions = totalTeamSubmissions;
    }

    public int getTotalSelfPages() {
        return totalSelfPages;
    }

    public void setTotalSelfPages(int totalSelfPages) {
        this.totalSelfPages = totalSelfPages;
    }

    public int getTotalTeamPages() {
        return totalTeamPages;
    }

    public void setTotalTeamPages(int totalTeamPages) {
        this.totalTeamPages = totalTeamPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
