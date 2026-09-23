package com.tripnest.backend.dto;

import com.tripnest.backend.model.Settlement;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {

    private Long id;
    private Long tripId;
    private Long payerId;
    private String payerName;
    private Long receiverId;
    private String receiverName;
    private Double amount;
    private Settlement.SettlementStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime settledAt;
}
