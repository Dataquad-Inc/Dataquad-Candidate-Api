package com.profile.candidate.repository;

import com.profile.candidate.model.PlacementDocs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlacementDocsRepository extends JpaRepository<PlacementDocs, Long> {
    List<PlacementDocs> findByPlacementDetails_IdAndIsDeletedFalse(String placementId);
}
