package com.tripnest.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SettlementResponse {
    private BigDecimal totalExpenses;
    private BigDecimal yourPaidAmount;
    private BigDecimal yourShare;
    private BigDecimal yourBalance;
    private List<MemberSummary> memberSummaries;

    @Data
    @Builder
    public static class MemberSummary {
        private Long userId;
        private String userName;
        private BigDecimal totalPaid;
        private BigDecimal totalShare;
        private BigDecimal balance;
    }
}
