package com.profile.candidate.repository;

import com.profile.candidate.dto.UserDetailsDTO;
import com.profile.candidate.dto.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface  UserDetailsProjectionRepository extends JpaRepository<UserDetailsEntity, String> {
    @Query(value = "SELECT MAX(CAST(user_id AS UNSIGNED)) FROM user_details", nativeQuery = true)
    Integer findMaxUserId();
}
