package com.tripnest.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationResponse {
    private Long id;
    private String name;
    private String country;
    private String city;
    private String description;
    private String imageUrl;
    private String bestTimeToVisit;
    private String category;
    private Boolean isPopular;
    private Long favoriteCount;
    private Boolean isFavorited; // If authenticated user has favorited it
    private LocalDateTime createdAt;
}
