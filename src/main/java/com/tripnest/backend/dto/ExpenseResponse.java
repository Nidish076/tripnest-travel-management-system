package com.tripnest.backend.dto;

import com.tripnest.backend.model.Expense;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {

    private Long id;
    private Long tripId;
    private String title;
    private Double amount;
    private Expense.ExpenseCategory category;
    private UserResponse paidBy;
    private String description;
    private LocalDate expenseDate;
    private List<ExpenseSplitResponse> splits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
