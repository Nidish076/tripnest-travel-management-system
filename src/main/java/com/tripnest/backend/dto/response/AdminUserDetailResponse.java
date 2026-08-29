package com.tripnest.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDetailResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private ProfileResponse profile;
    private TravelPreferenceResponse travelPreferences;
    private AccountSettingResponse accountSettings;
    private Integer favoriteDestinationsCount;
}
