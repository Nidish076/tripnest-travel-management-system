package com.tripnest.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ExpenseRequest {
    @NotBlank
    private String description;

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    private String category;

    @NotNull
    private LocalDate expenseDate;

    @NotNull
    private Long paidBy;

    private List<Long> participants;
}
