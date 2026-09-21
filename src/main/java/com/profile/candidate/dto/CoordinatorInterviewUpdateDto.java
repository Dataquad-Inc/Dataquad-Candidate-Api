package com.profile.candidate.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CoordinatorInterviewUpdateDto {

    private String interviewStatus;

    @JsonProperty("internalFeedBack")
    @JsonAlias({"internalFeedback", "internal_feedback"})
    private String internalFeedBack;

    private boolean skipNotification;

    public boolean isSkipNotification() {
        return skipNotification;
    }

    public void setSkipNotification(boolean skipNotification) {
        this.skipNotification = skipNotification;
    }

    public String getInterviewStatus() {
        return interviewStatus;
    }

    public void setInterviewStatus(String interviewStatus) {
        this.interviewStatus = interviewStatus;
    }

    public String getInternalFeedBack() {
        return internalFeedBack;
    }

    public void setInternalFeedBack(String internalFeedBack) {
        this.internalFeedBack = internalFeedBack;
    }
}