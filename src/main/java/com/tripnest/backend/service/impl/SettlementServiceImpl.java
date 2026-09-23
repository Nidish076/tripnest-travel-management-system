package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.SettlementRequest;
import com.tripnest.backend.dto.SettlementResponse;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.model.Settlement;
import com.tripnest.backend.model.Trip;
import com.tripnest.backend.model.TripMember;
import com.tripnest.backend.model.User;
import com.tripnest.backend.repository.SettlementRepository;
import com.tripnest.backend.repository.TripMemberRepository;
import com.tripnest.backend.repository.TripRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SettlementResponse createSettlement(Long tripId, SettlementRequest request, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You must be a member of this trip to record settlements"));

        if (request.getPayerId().equals(request.getReceiverId())) {
            throw new BadRequestException("Payer and receiver cannot be the same user");
        }

        User payer = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found with id: " + request.getPayerId()));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + request.getReceiverId()));

        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, request.getPayerId())) {
            throw new BadRequestException("Payer is not a member of this trip");
        }

        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, request.getReceiverId())) {
            throw new BadRequestException("Receiver is not a member of this trip");
        }

        Settlement settlement = Settlement.builder()
                .trip(trip)
                .payer(payer)
                .receiver(receiver)
                .amount(round(request.getAmount()))
                .status(Settlement.SettlementStatus.PENDING)
                .build();

        Settlement saved = settlementRepository.save(settlement);
        return mapToSettlementResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getTripSettlements(Long tripId, Long currentUserId) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        return settlementRepository.findByTripIdOrderByCreatedAtDesc(tripId).stream()
                .map(this::mapToSettlementResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SettlementResponse completeSettlement(Long settlementId, Long currentUserId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found with id: " + settlementId));

        Long tripId = settlement.getTrip().getId();
        TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        boolean isParticipant = settlement.getReceiver().getId().equals(currentUserId) || settlement.getPayer().getId().equals(currentUserId);
        boolean isPrivileged = member.getRole() == TripMember.MemberRole.OWNER || member.getRole() == TripMember.MemberRole.ADMIN;

        if (!isParticipant && !isPrivileged) {
            throw new AccessDeniedException("You do not have permission to complete this settlement");
        }

        if (settlement.getStatus() == Settlement.SettlementStatus.COMPLETED) {
            throw new BadRequestException("Settlement is already marked as completed");
        }

        settlement.setStatus(Settlement.SettlementStatus.COMPLETED);
        settlement.setSettledAt(LocalDateTime.now());

        Settlement updated = settlementRepository.save(settlement);
        return mapToSettlementResponse(updated);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private SettlementResponse mapToSettlementResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .tripId(settlement.getTrip().getId())
                .payerId(settlement.getPayer().getId())
                .payerName(settlement.getPayer().getName())
                .receiverId(settlement.getReceiver().getId())
                .receiverName(settlement.getReceiver().getName())
                .amount(settlement.getAmount())
                .status(settlement.getStatus())
                .createdAt(settlement.getCreatedAt())
                .settledAt(settlement.getSettledAt())
                .build();
    }
}
