package com.tripnest.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripBalancesResponse {

    private Long tripId;
    private Double totalExpenses;
    private List<MemberBalanceResponse> members;
    private List<PaymentSuggestionResponse> settlementSuggestions;
}
