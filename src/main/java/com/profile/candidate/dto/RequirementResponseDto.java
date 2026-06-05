package com.profile.candidate.dto;

public class RequirementResponseDto {

    private Requirement requirement;

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(
            Requirement requirement
    ) {
        this.requirement = requirement;
    }

    public static class Requirement {

        private String jobId;
        private String jobTitle;
        private String jobDescription;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(
                String jobId
        ) {
            this.jobId = jobId;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public void setJobTitle(
                String jobTitle
        ) {
            this.jobTitle = jobTitle;
        }

        public String getJobDescription() {
            return jobDescription;
        }

        public void setJobDescription(
                String jobDescription
        ) {
            this.jobDescription = jobDescription;
        }
    }
}