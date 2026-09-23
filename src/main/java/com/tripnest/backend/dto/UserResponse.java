package com.tripnest.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String profileImage;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
