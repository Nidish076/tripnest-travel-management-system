package com.tripnest.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String profilePhoto;
    private Integer age;
    private String location;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
