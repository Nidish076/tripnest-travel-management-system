package com.tripnest.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupMemberResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private LocalDateTime joinedAt;
}
