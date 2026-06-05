package com.profile.candidate.client;

import com.profile.candidate.dto.RequirementResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "requirements-service",
        url = "${requirements.service.url}"
)
public interface RequirementClient {

    @GetMapping("/{id}")
    ResponseEntity<RequirementResponseDto>
    getRequirementById(
            @PathVariable("id")
            String requirementId
    );
}