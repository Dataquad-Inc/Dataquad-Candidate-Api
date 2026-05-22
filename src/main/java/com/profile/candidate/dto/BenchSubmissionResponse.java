package com.profile.candidate.dto;

import java.util.List;

public class BenchSubmissionResponse {

    private String message;
    private String jobId;
    private List<String> submittedBenchIds;
    private List<String> skippedBenchIds;

    public BenchSubmissionResponse(
            String message,
            String jobId,
            List<String> submittedBenchIds,
            List<String> skippedBenchIds
    ) {
        this.message = message;
        this.jobId = jobId;
        this.submittedBenchIds = submittedBenchIds;
        this.skippedBenchIds = skippedBenchIds;
    }

    public String getMessage() {
        return message;
    }

    public String getJobId() {
        return jobId;
    }

    public List<String> getSubmittedBenchIds() {
        return submittedBenchIds;
    }

    public List<String> getSkippedBenchIds() {
        return skippedBenchIds;
    }
}