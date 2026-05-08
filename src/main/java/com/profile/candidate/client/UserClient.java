package com.profile.candidate.client;

import com.profile.candidate.dto.ApiResponse;
import com.profile.candidate.dto.UserDetailsDTO;
import com.profile.candidate.dto.UserLoginStatusDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "user-service", url = "http://dataquad-userregister-prod:8083/users")
public interface UserClient {

    @PostMapping("/register")
    ResponseEntity<ApiResponse<UserDetailsDTO>> registerUser(@RequestBody UserDetailsDTO userDto);

    @GetMapping("/email")
    ResponseEntity<ApiResponse<UserDetailsDTO>> getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/{userId}/login-status")
    ResponseEntity<ApiResponse<UserLoginStatusDTO>> getLoginStatusByUserId(@PathVariable("userId") String userId);

}
