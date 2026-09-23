package com.tripnest.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String location;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String startTime;

    private String endTime;

    @PositiveOrZero(message = "Estimated cost must be zero or a positive amount")
    private Double estimatedCost;
}
