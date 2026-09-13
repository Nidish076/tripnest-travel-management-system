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

    private String currency;
    private String language;
    private String climate;
    private String transportation;
    private String visaRequirements;
    private String timeZone;
    private java.util.List<String> travelTips;
    private java.util.List<AttractionResponse> attractions;

    private LocalDateTime createdAt;
}
