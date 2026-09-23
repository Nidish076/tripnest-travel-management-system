package com.tripnest.backend.service;

import com.tripnest.backend.dto.JoinTripRequest;
import com.tripnest.backend.dto.TripMemberResponse;

import java.util.List;

public interface MemberService {
    TripMemberResponse joinTrip(Long tripId, JoinTripRequest request, Long currentUserId);
    List<TripMemberResponse> getTripMembers(Long tripId, Long currentUserId);
    void removeMember(Long tripId, Long targetUserId, Long currentUserId);
}
