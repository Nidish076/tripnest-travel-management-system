package com.tripnest.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InvitationResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long invitedUserId;
    private String invitedUserName;
    private Long invitedById;
    private String invitedByName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
