package com.tripnest.backend.dto;

import com.tripnest.backend.model.TripMember;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMemberResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String profileImage;
    private TripMember.MemberRole role;
    private LocalDateTime joinedAt;
}
