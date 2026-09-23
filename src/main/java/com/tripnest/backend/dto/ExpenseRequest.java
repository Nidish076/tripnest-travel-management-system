package com.tripnest.backend.dto;

import com.tripnest.backend.model.Expense;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequest {

    @NotBlank(message = "Expense title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    @NotNull(message = "Category is required")
    private Expense.ExpenseCategory category;

    private String description;

    private LocalDate expenseDate;

    private Long paidBy;

    private List<@Valid ExpenseSplitRequest> splits;
}
