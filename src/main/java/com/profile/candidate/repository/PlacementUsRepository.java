package com.profile.candidate.repository;

import com.profile.candidate.model.PlacementDetailsUS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacementUsRepository extends JpaRepository<PlacementDetailsUS, String> {
    boolean existsByCandidateEmailId(String candidateEmailId);
    boolean existsByCandidateContactNo(String candidateContactNo);
    boolean existsByInterviewId(String interviewId);
    PlacementDetailsUS findByCandidateContactNoAndClientName(String candidateContactNo, String clientName);
    java.util.List<PlacementDetailsUS> findByUserId(String userId);
    Page<PlacementDetailsUS> findByUserId(String userId, Pageable pageable);
    
    @Query("SELECT p FROM PlacementDetailsUS p WHERE " +
           "(:search IS NULL OR LOWER(p.candidateFullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.candidateEmailId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.technology) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PlacementDetailsUS> searchUsPlacements(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM PlacementDetailsUS p WHERE " +
           "p.userId = :userId AND " +
           "(:search IS NULL OR LOWER(p.candidateFullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.candidateEmailId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.technology) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PlacementDetailsUS> searchUsPlacements(@Param("userId") String userId, @Param("search") String search, Pageable pageable);
}
