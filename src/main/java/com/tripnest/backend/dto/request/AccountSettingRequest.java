package com.tripnest.backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountSettingRequest {
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean promoEmails;
    private Boolean isAccountActive;
}
