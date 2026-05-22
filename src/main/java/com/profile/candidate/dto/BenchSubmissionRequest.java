package com.profile.candidate.dto;

import java.util.List;

public class BenchSubmissionRequest {

    private List<String> benchIds;
    private String jobId;

    public List<String> getBenchIds() {
        return benchIds;
    }

    public void setBenchIds(List<String> benchIds) {
        this.benchIds = benchIds;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}