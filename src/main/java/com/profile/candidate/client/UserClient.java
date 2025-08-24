package com.profile.candidate.client;

import com.profile.candidate.dto.ApiResponse;
import com.profile.candidate.dto.UserDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "http://dataquad-userregister-prod:8083/users")
public interface UserClient {

    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserDetailsDTO>> registerUser(@RequestBody UserDetailsDTO userDto);

}
