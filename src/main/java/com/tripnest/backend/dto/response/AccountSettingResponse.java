package com.tripnest.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSettingResponse {
    private Long id;
    private Long userId;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean promoEmails;
    private Boolean isAccountActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
