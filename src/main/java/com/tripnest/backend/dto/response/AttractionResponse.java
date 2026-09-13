package com.tripnest.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionResponse {
    private Long id;
    private Long destinationId;
    private String name;
    private String description;
    private String imageUrl;
    private String location;
    private String category;
    private Double entryFee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
