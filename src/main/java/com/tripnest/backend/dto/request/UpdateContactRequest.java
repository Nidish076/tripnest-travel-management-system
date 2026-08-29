package com.tripnest.backend.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateContactRequest {

    @Email(message = "Email must be a valid email address")
    private String email;

    private String phone;
}
