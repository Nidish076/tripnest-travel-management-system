package com.tripnest.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private String description;
    private Double amount;
    private String category;
    private LocalDate expenseDate;
    private Long paidById;
    private String paidByName;
    private LocalDateTime createdAt;
    private List<ExpenseParticipantResponse> participants;
}
