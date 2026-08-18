package com.profile.candidate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<String> findEmployeeNamesByRole(String role) {

        String sql = """
            SELECT u.user_name
            FROM user_details u
            JOIN user_roles ur ON u.user_id = ur.user_id
            JOIN roles r ON ur.role_id = r.id
            WHERE r.name = ?
            """;

        return jdbcTemplate.queryForList(
            sql,
            String.class,
            role
        );
    }
}
