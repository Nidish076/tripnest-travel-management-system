package com.tripnest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinTripRequest {

    @NotBlank(message = "Invite code is required")
    private String inviteCode;
}
