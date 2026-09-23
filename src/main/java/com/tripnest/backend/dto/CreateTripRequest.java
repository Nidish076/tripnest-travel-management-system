package com.tripnest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTripRequest {

    @NotBlank(message = "Trip name is required")
    @Size(min = 2, max = 150, message = "Trip name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Destination is required")
    private String destination;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @PositiveOrZero(message = "Budget must be zero or a positive amount")
    private Double budget;
}
