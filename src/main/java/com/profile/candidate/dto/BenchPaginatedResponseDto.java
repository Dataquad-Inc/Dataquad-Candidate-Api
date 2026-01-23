package com.profile.candidate.dto;

import java.util.List;

public class BenchPaginatedResponseDto {
    private List<BenchDetailsDto> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public BenchPaginatedResponseDto(List<BenchDetailsDto> content, long totalElements, int totalPages, int currentPage, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    // Getters and setters
    public List<BenchDetailsDto> getContent() {
        return content;
    }

    public void setContent(List<BenchDetailsDto> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
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