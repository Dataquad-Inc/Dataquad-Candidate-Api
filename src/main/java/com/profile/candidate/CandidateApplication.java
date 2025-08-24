package com.profile.candidate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = "com.profile.candidate")
@EnableFeignClients(basePackages = "com.profile.candidate.client")
public class CandidateApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandidateApplication.class, args);
	}

}
