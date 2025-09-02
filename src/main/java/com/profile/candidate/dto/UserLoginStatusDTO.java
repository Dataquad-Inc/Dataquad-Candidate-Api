package com.profile.candidate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class UserLoginStatusDTO {
    private String userId;
    private LocalDateTime lastLoginTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    // Correct getter method for boolean must be named isLogin()
    @JsonProperty("isLogin")
    public boolean isLogin() {
        // Return true if lastLoginTime is not null
        return lastLoginTime != null;
    }


}
