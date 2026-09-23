package com.tripnest.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSuggestionResponse {

    private Long payerId;
    private String payer;
    private Long receiverId;
    private String receiver;
    private Double amount;
}
