package com.tripnest.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private int memberCount;
    private String currentUserRole;
}
