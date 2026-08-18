package com.profile.candidate.repository;

import com.profile.candidate.model.UserDetails;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<UserDetails, Integer> {

  @Query("""
          SELECT u.userName
          FROM UserDetails u
          JOIN u.roles r
          WHERE r.roleName = :role
      """)
  List<String> findEmployeeNamesByRole(@Param("role") String role);
}
