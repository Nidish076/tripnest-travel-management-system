package com.tripnest.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplitResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Double amount;
    private Boolean settled;
}
