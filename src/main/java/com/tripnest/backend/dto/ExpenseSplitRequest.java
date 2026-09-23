package com.tripnest.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplitRequest {

    @NotNull(message = "User ID is required for split")
    private Long userId;

    @NotNull(message = "Split amount is required")
    @Positive(message = "Split amount must be greater than zero")
    private Double amount;
}
