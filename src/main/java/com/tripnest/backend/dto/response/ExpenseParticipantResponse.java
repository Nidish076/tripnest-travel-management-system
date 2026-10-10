package com.tripnest.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ExpenseParticipantResponse {
    private Long userId;
    private String userName;
    private BigDecimal shareAmount;
    private boolean settled;
}
