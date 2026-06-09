package com.profile.candidate.repository;
import com.profile.candidate.dto.BenchDetailsDto;
import com.profile.candidate.model.BenchDetails;
import com.profile.candidate.model.CandidateDetails;
import com.profile.candidate.model.Submissions;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BenchRepository extends JpaRepository<BenchDetails, String> {
    Page<BenchDetails> findAll(Pageable pageable);

    @Query("SELECT b FROM BenchDetails b " +
            "WHERE LOWER(b.id) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.technology) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.referredBy) LIKE LOWER(CONCAT('%', :search, '%'))"+
            "OR LOWER(b.tags) LIKE LOWER(CONCAT('%', :search, '%'))")

    Page<BenchDetails> searchBenchDetails(@Param("search") String search, Pageable pageable);

    Optional<BenchDetails> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByFullName(String fullName);
    boolean existsByContactNumber(String contactNumber);

    // ✅ Check for duplicates **excluding** a specific ID (for updates)
    boolean existsByFullNameAndIdNot(String fullName, String id);
    boolean existsByEmailAndIdNot(String email, String id);
    boolean existsByContactNumberAndIdNot(String contactNumber, String id);

    List<BenchDetails> findByReferredBy(String referredBy);  // ✅ Add this method

    @Query("DELETE FROM BenchDetails b WHERE LOWER(b.id) = LOWER(:id)")
    @Modifying
    @Transactional
    void deleteByIdIgnoreCase(@Param("id") String id);

    @Query("""
            SELECT DISTINCT b.tags
            FROM BenchDetails b
            WHERE b.tags IS NOT NULL
            AND b.tags <> ''
            ORDER BY b.tags ASC
            """)
    List<String> getAllTags();

    @Query("""
            SELECT b.tags, COUNT(b)
            FROM BenchDetails b
            WHERE b.tags IS NOT NULL
            AND b.tags <> ''
            GROUP BY b.tags
            ORDER BY LOWER(TRIM(b.tags)) ASC""")
    List<Object[]> getTagCounts();

    @Query("""
       SELECT new com.profile.candidate.dto.BenchDetailsDto(
           b.id,
           b.fullName,
           b.email,
           b.relevantExperience,
           b.totalExperience,
           b.contactNumber,
           b.skills,
           b.linkedin,
           b.referredBy,
           b.createdDate,
           b.technology,
           b.remarks,
           b.tags
       )
       FROM BenchDetails b
       WHERE LOWER(b.tags) LIKE LOWER(CONCAT('%', :tagName, '%'))
       """)
    List<BenchDetailsDto> findBenchProfilesByTag(
            @Param("tagName") String tagName);

    @Query("SELECT b FROM BenchDetails b WHERE b.createdDate BETWEEN :startDate AND :endDate")
    List<BenchDetails> findByCreatedDateBetween(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    List<BenchDetails> findByIdIn(List<String> benchIds);


    @Query(value = """
            SELECT MAX(CAST(SUBSTRING(id, 6) AS UNSIGNED))
            FROM bench_details
            WHERE id LIKE 'BENCH%'
            """, nativeQuery = true)
    Integer findMaxBenchNumber();

    boolean existsByEmailOrContactNumber(
            String email,
            String contactNumber
    );
}