package com.profile.candidate.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_details")
public class UserDetailsEntity {
    @Id
    @Column(name = "user_id")
    private String userId;


    // Add only required fields (just userId is enough here)

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
