package com.tripnest.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttractionRequest {

    @NotBlank(message = "Attraction name is required")
    private String name;

    private String description;

    private String imageUrl;

    private String location;

    private String category;

    @PositiveOrZero(message = "Entry fee must be zero or positive")
    private Double entryFee;
}
