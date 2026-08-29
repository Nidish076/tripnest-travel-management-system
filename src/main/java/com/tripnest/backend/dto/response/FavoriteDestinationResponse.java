package com.tripnest.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDestinationResponse {
    private Long id;
    private Long userId;
    private DestinationResponse destination;
    private LocalDateTime favoritedAt;
}
