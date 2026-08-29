package com.tripnest.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPreferenceResponse {
    private Long id;
    private Long userId;
    private String preferredTravelType;
    private List<String> preferredDestinations;
    private String budgetRange;
    private List<String> preferredActivities;
    private String preferredTransportation;
    private String preferredAccommodationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
