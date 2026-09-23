package com.tripnest.backend.dto;

import com.tripnest.backend.model.TripMember;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {

    private Long id;
    private String name;
    private String destination;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;
    private String inviteCode;
    private UserResponse createdBy;
    private Integer memberCount;
    private TripMember.MemberRole userRole;
    private List<TripMemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
