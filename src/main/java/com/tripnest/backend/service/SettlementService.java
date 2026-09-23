package com.tripnest.backend.service;

import com.tripnest.backend.dto.SettlementRequest;
import com.tripnest.backend.dto.SettlementResponse;

import java.util.List;

public interface SettlementService {
    SettlementResponse createSettlement(Long tripId, SettlementRequest request, Long currentUserId);
    List<SettlementResponse> getTripSettlements(Long tripId, Long currentUserId);
    SettlementResponse completeSettlement(Long settlementId, Long currentUserId);
}
