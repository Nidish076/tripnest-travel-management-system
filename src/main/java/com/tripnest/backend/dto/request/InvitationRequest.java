package com.tripnest.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvitationRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;
}
