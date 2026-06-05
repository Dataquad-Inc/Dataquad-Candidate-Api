package com.profile.candidate.dto;
import java.util.List;

public class SendJdRequest {

    private String requirementId;
    private List<String> benchIds;

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(
            String requirementId
    ) {
        this.requirementId = requirementId;
    }

    public List<String> getBenchIds() {
        return benchIds;
    }

    public void setBenchIds(
            List<String> benchIds
    ) {
        this.benchIds = benchIds;
    }
}
