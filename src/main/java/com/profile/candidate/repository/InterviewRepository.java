package com.profile.candidate.repository;

import com.profile.candidate.model.InterviewDetails;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<InterviewDetails,String> {


    @Query(value = "SELECT b.id FROM bdm_client AS b " +
            "JOIN requirements_model AS r " +
            "ON r.client_name LIKE CONCAT(b.client_name, '%') " +
            "WHERE r.client_name = :clientName " +
            "LIMIT 1", nativeQuery = true)
    String findClientIdByClientName(@Param("clientName") String clientName);

    @Query("SELECT i FROM InterviewDetails i WHERE i.assignedTo = :userId AND i.timestamp BETWEEN :startDateTime AND :endDateTime")
    List<InterviewDetails> findScheduledInterviewsByAssignedToAndDateRange(
            @Param("userId") String userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query(value = "SELECT email FROM user_details  " +
            "WHERE user_id = :userId ", nativeQuery = true)
    String findUserEmailByUserId(@Param("userId") String userId);

    InterviewDetails findByCandidateIdAndUserId(String candidateId, String userId);

    @Query("SELECT i FROM InterviewDetails i WHERE i.candidateId = :candidateId")
    List<InterviewDetails> findInterviewsByCandidateId(@Param("candidateId") String candidateId);

    InterviewDetails findByCandidateIdAndUserIdAndClientName(String candidateId, String userId, String clientName);

    InterviewDetails findByCandidateIdAndUserIdAndClientNameAndJobId(String candidateId, String userId, String clientName, String jobId);

    InterviewDetails findInterviewsByCandidateIdAndJobId(String candidateId, String jobId);

    InterviewDetails findByCandidateIdAndUserIdAndJobId(String candidateId, String userId, String jobId);

    Optional<InterviewDetails> findByCandidateIdAndJobIdAndInterviewDateTime(String candidateId, String jobId, OffsetDateTime interviewDateTime);

    List<InterviewDetails> findByUserId(String userId);

    InterviewDetails findByCandidateIdAndClientNameAndJobId(String candidateId, String clientName, String jobId);

    InterviewDetails findByCandidateIdAndJobId(String candidateId, String jobId);

    InterviewDetails findByCandidateIdAndClientName(String candidateId, String clientName);

    @Query(value = "SELECT r.job_title FROM requirements_model r WHERE r.job_id = :jobId", nativeQuery = true)
    String findJobTitleByJobId(@Param("jobId") String jobId);

    @Query(value = "SELECT user_name FROM user_details WHERE user_id = :userId", nativeQuery = true)
    String findUsernameByUserId(@Param("userId") String userId);

    @Query("SELECT i FROM InterviewDetails i WHERE i.userId = :userId AND i.timestamp BETWEEN :startDateTime AND :endDateTime")
    List<InterviewDetails> findScheduledInterviewsByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);


    @Query("SELECT i FROM InterviewDetails i " +
            "WHERE i.interviewDateTime IS NOT NULL " +
            "AND FUNCTION('DATE', i.timestamp) BETWEEN :startDate AND :endDate")
    List<InterviewDetails> findScheduledInterviewsByDateOnly(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    @Query(value = """
                SELECT r.name
                FROM user_roles ur
                JOIN roles r ON ur.role_id = r.id
                WHERE ur.user_id = :userId
                LIMIT 1
            """, nativeQuery = true)
    String findRoleByUserId(@Param("userId") String userId);

    @Query(value = """
    SELECT 
        c.job_id,
        c.interview_id AS interview_id,
        c.candidate_id,
        c.full_name,
        c.contact_number,
        c.candidate_email_id,
        c.user_email,
        c.user_id,
        DATE_FORMAT(c.interview_date_time, '%Y-%m-%dT%H:%i:%s') AS interview_date_time,
        c.duration,
        c.zoom_link,
        DATE_FORMAT(c.timestamp, '%Y-%m-%dT%H:%i:%s') AS timestamp,
        c.client_email,
        c.client_name,
        c.interview_level,
        c.interview_status,
        c.is_placed,
        r2.job_title AS technlogy,
        c.recruiter_name AS recruiterName,
        c.internal_feedback AS internalFeedback,
        c.comments AS comments,
        cd.total_experience,
        cd.relevant_experience,
        s.skills
    FROM 
        interview_details c
    JOIN requirements_model r2 ON r2.job_id = c.job_id
    LEFT JOIN candidates cd ON cd.candidate_id = c.candidate_id
    LEFT JOIN candidate_submissions s ON s.candidate_id = c.candidate_id AND s.job_id = c.job_id
    WHERE 
        c.job_id IN (
           SELECT r.job_id
           FROM requirements_model r
            JOIN bdm_client b 
                ON TRIM(UPPER(r.client_name)) COLLATE utf8mb4_bin = TRIM(UPPER(b.client_name)) COLLATE utf8mb4_bin
            JOIN user_details u 
                ON b.on_boarded_by = u.user_name
            WHERE u.user_id = :userId
        )
        AND c.interview_date_time IS NOT NULL
        AND c.timestamp BETWEEN :startDateTime AND :endDateTime
    """, nativeQuery = true)
    List<Tuple> findScheduledInterviewsByBdmUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM InterviewDetails i " +
            "WHERE i.assignedTo = :userId AND i.timestamp BETWEEN :start AND :end")
    boolean existsByAssignedToAndDateRange(@Param("userId") String userId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);



    @Query(value = """
                SELECT c.* 
                FROM interview_details c
                JOIN requirements_model r ON c.job_id = r.job_id
                WHERE c.user_id = :userId
                  AND c.interview_date_time IS NOT NULL
                  AND c.client_name = r.client_name  -- Ensures client_name matches between candidates and requirements
                  AND DATE(c.timestamp) BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<InterviewDetails> findSelfScheduledInterviewsByTeamleadAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
                SELECT c.* 
                FROM user_details u
                JOIN requirements_model r ON r.assigned_by = u.user_name
                JOIN interview_details c ON c.job_id = r.job_id
                WHERE u.user_id = :userId
                  AND c.user_id != u.user_id
                  AND c.interview_date_time IS NOT NULL
                  AND c.job_id IN (
                      SELECT r2.job_id 
                      FROM requirements_model r2
                      WHERE r2.client_name = r.client_name 
                        AND r2.assigned_by = u.user_name
                  )  -- Ensures client_name matches between candidates and requirements
                  AND DATE(c.timestamp) BETWEEN :startDate AND :endDate  -- Date only filter using timestamp
            """, nativeQuery = true)
    List<InterviewDetails> findTeamScheduledInterviewsByTeamleadAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    InterviewDetails findByCandidateId(String candidateId);

    Optional<InterviewDetails> findByContactNumberAndCandidateEmailIdAndJobId(String candidateContactNo, String candidateEmailId,String jobId);

    List<InterviewDetails> findByAssignedTo(String userId);

    InterviewDetails findByInterviewIdAndAssignedTo(String interviewId,String coordinatorId);
    List<InterviewDetails> findByCandidateIdOrderByTimestampDesc(String candidateId);

    @Query("SELECT DISTINCT i.candidateId FROM InterviewDetails i")
    List<String> findAllCandidateIdsWithInterviews();


    @Query(value = """
    SELECT DISTINCT id.candidate_id, id.job_id
    FROM interview_details id
    WHERE id.interview_status IS NOT NULL
      AND id.interview_status != ''
      AND JSON_VALID(id.interview_status)
      AND NOT (
          JSON_UNQUOTE(JSON_EXTRACT(id.interview_status, CONCAT(
              '$[', JSON_LENGTH(id.interview_status) - 1, '].status'))) = 'REJECTED'
          AND JSON_UNQUOTE(JSON_EXTRACT(id.interview_status, CONCAT(
              '$[', JSON_LENGTH(id.interview_status) - 1, '].interviewLevel'))) = 'INTERNAL'
      )
    """, nativeQuery = true)
    List<String> findInternalRejectedCandidateIdsLatestOnly();
}




