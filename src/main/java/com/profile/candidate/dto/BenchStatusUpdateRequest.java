package com.profile.candidate.dto;

import com.profile.candidate.model.BenchStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BenchStatusUpdateRequest {

    private BenchStatus status;
}
