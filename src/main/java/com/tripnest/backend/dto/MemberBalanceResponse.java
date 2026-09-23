package com.tripnest.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBalanceResponse {

    private Long userId;
    private String name;
    private String email;
    private String profileImage;
    private Double totalPaid;
    private Double totalOwed;
    private Double balance;
}
