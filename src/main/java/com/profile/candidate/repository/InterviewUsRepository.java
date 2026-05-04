package com.profile.candidate.repository;

import com.profile.candidate.model.InterviewDetailsUS;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewUsRepository extends JpaRepository<InterviewDetailsUS, String> {
}
