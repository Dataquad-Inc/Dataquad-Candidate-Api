package com.profile.candidate.repository;

import com.profile.candidate.model.CandidateDetails;
import com.profile.candidate.model.Submissions;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submissions,String> {


     List<Submissions> findByCandidate_CandidateId(String candidateId);

     Submissions findByCandidate_CandidateIdAndJobId(String candidateId, String jobId);

    Optional<Submissions> findBySubmissionIdAndUserId(String candidateId, String userId);

    List<Submissions> findByCandidate_CandidateIdIn(List<String> candidateIds);

     List<Submissions> findByCandidate(CandidateDetails candidate);

    Submissions findByCandidate_ContactNumberAndJobId(String contactNumber, String jobId);

    Submissions findByCandidate_CandidateEmailIdAndJobId(String candidateId, String jobId);

    @Query("SELECT s.candidate.candidateId FROM Submissions s WHERE s.submissionId = :submissionId")
    String findCandidateIdBySubmissionId(@Param("submissionId") String submissionId);

    @Query(value = """
    SELECT r.name
    FROM user_roles ur
    JOIN roles r ON ur.role_id = r.id
    WHERE ur.user_id = :userId
    LIMIT 1
""", nativeQuery = true)
    String findRoleByUserId(@Param("userId") String userId);
    @Query("SELECT s FROM Submissions s  WHERE s.userId = :userId AND s.profileReceivedDate BETWEEN :startDate AND :endDate")
    List<Submissions> findByUserIdAndProfileReceivedDateBetween(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
    SELECT * FROM candidate_submissions c
    WHERE c.job_id IN (
        SELECT r.job_id
        FROM requirements_model r
        JOIN bdm_client b 
            ON TRIM(UPPER(r.client_name)) COLLATE utf8mb4_bin = TRIM(UPPER(b.client_name)) COLLATE utf8mb4_bin
        JOIN user_details u 
            ON b.on_boarded_by = u.user_name
        WHERE u.user_id = :userId
    )
    AND c.profile_received_date BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    List<Submissions> findSubmissionsByBdmUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    List<Submissions> findByProfileReceivedDateBetween(LocalDate start, LocalDate end);


    @Query(value = """    
	SELECT 
	cs.submission_id,     
	cs.candidate_id AS candidate_id,       
	cs.recruiter_name as recruiter_name,   
	c.full_name AS full_name,    
    cs.skills AS skills,      
	cs.job_id AS job_id,
	c.user_id AS user_id,
	c.user_email AS user_email,
	cs.preferred_location AS preferred_location,
	DATE_FORMAT(cs.profile_received_date, '%Y-%m-%d') AS profile_received_date,
	r.job_title AS job_title,    
    r.client_name AS client_name,
	c.contact_number AS contact_number,      
    c.candidate_email_id AS candidate_email_id,  
	c.total_experience AS total_experience,   
	c.relevant_experience AS relevant_experience 
	FROM user_details u 
	JOIN requirements_model r ON r.assigned_by = u.user_name  
	JOIN candidate_submissions cs ON cs.job_id = r.job_id    
	JOIN candidates c ON c.candidate_id = cs.candidate_id   
	WHERE u.user_id = :userId   AND c.user_id != u.user_id   
	AND cs.profile_received_date BETWEEN :startDate AND :endDate AND 
	cs.job_id IN (SELECT r2.job_id FROM requirements_model r2 WHERE r2.assigned_by = u.user_name)""", nativeQuery = true)
    List<Tuple> findTeamSubmissionsByTeamleadAndDateRange( @Param("userId") String userId,@Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate);

    @Query(value = """    
	SELECT         
	cs.submission_id,        
	cs.recruiter_name as recruiter_name,       
	cs.candidate_id AS candidate_id,        
	c.full_name AS full_name,       
	c.contact_number AS contact_number,  -- Added contact_number field      
	c.candidate_email_id AS candidate_email_id,  -- Added candidate email       
	cs.skills AS skills,        
	cs.job_id AS job_id,        
	c.user_id AS user_id,  
	c.user_email AS user_email,        
	cs.preferred_location AS preferred_location,   
	DATE_FORMAT(cs.profile_received_date, '%Y-%m-%d') AS profile_received_date,   
	r.job_title AS job_title,       
	r.client_name AS client_name,    
    c.total_experience AS total_experience,  -- Added total_experience field
	c.relevant_experience AS relevant_experience  -- Added relevant_experience field   
	FROM candidates c     
	JOIN candidate_submissions cs ON c.candidate_id = cs.candidate_id  
	JOIN requirements_model r ON cs.job_id = r.job_id    
	WHERE c.user_id = :userId     
	AND cs.profile_received_date BETWEEN :startDate AND :endDate""", nativeQuery = true)
    List<Tuple> findSelfSubmissionsByTeamleadAndDateRange( @Param("userId") String userId,@Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate);


    List<Submissions> findByJobId(String jobId);
}
