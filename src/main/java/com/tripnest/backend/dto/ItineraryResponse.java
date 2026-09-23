package com.tripnest.backend.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryResponse {

    private Long id;
    private Long tripId;
    private String title;
    private String description;
    private String location;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private Double estimatedCost;
    private UserResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
