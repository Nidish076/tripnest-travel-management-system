package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.*;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.model.*;
import com.tripnest.backend.repository.*;
import com.tripnest.backend.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final SettlementRepository settlementRepository;

    private static final String INVITE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public TripResponse createTrip(CreateTripRequest request, Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new BadRequestException("Trip end date cannot be before start date");
            }
        }

        String inviteCode = generateUniqueInviteCode();

        Trip trip = Trip.builder()
                .name(request.getName().trim())
                .destination(request.getDestination().trim())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget() != null ? request.getBudget() : 0.0)
                .inviteCode(inviteCode)
                .createdBy(user)
                .build();

        Trip savedTrip = tripRepository.save(trip);

        TripMember ownerMember = TripMember.builder()
                .trip(savedTrip)
                .user(user)
                .role(TripMember.MemberRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        tripMemberRepository.save(ownerMember);

        return mapToTripResponse(savedTrip, TripMember.MemberRole.OWNER, List.of(mapToMemberResponse(ownerMember)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getUserTrips(Long currentUserId) {
        List<Trip> trips = tripRepository.findTripsByUserId(currentUserId);

        return trips.stream().map(trip -> {
            TripMember member = tripMemberRepository.findByTripIdAndUserId(trip.getId(), currentUserId)
                    .orElse(null);
            TripMember.MemberRole role = member != null ? member.getRole() : null;
            List<TripMember> allMembers = tripMemberRepository.findByTripId(trip.getId());
            List<TripMemberResponse> memberResponses = allMembers.stream()
                    .map(this::mapToMemberResponse)
                    .collect(Collectors.toList());

            return mapToTripResponse(trip, role, memberResponses);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        List<TripMember> allMembers = tripMemberRepository.findByTripId(tripId);
        List<TripMemberResponse> memberResponses = allMembers.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());

        return mapToTripResponse(trip, member.getRole(), memberResponses);
    }

    @Override
    @Transactional
    public TripResponse updateTrip(Long tripId, UpdateTripRequest request, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        if (member.getRole() != TripMember.MemberRole.OWNER && member.getRole() != TripMember.MemberRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can update trip details");
        }

        if (request.getStartDate() != null && request.getEndDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new BadRequestException("Trip end date cannot be before start date");
            }
        }

        trip.setName(request.getName().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDescription(request.getDescription());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        if (request.getBudget() != null) {
            trip.setBudget(request.getBudget());
        }

        Trip updatedTrip = tripRepository.save(trip);
        List<TripMember> allMembers = tripMemberRepository.findByTripId(tripId);
        List<TripMemberResponse> memberResponses = allMembers.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());

        return mapToTripResponse(updatedTrip, member.getRole(), memberResponses);
    }

    @Override
    @Transactional
    public void deleteTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        if (member.getRole() != TripMember.MemberRole.OWNER) {
            throw new AccessDeniedException("Only the OWNER can delete the trip");
        }

        tripRepository.delete(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripBalancesResponse getTripBalances(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        List<TripMember> members = tripMemberRepository.findByTripId(tripId);
        List<Expense> expenses = expenseRepository.findByTripId(tripId);
        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseTripId(tripId);
        List<Settlement> settlements = settlementRepository.findByTripId(tripId);

        double totalExpenses = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        Map<Long, Double> paidMap = new HashMap<>();
        Map<Long, Double> owedMap = new HashMap<>();
        Map<Long, Double> settlementAdjustmentMap = new HashMap<>();

        for (TripMember tm : members) {
            paidMap.put(tm.getUser().getId(), 0.0);
            owedMap.put(tm.getUser().getId(), 0.0);
            settlementAdjustmentMap.put(tm.getUser().getId(), 0.0);
        }

        for (Expense expense : expenses) {
            Long payerId = expense.getPaidBy().getId();
            paidMap.put(payerId, paidMap.getOrDefault(payerId, 0.0) + expense.getAmount());
        }

        for (ExpenseSplit split : splits) {
            Long splitUserId = split.getUser().getId();
            owedMap.put(splitUserId, owedMap.getOrDefault(splitUserId, 0.0) + split.getAmount());
        }

        for (Settlement s : settlements) {
            if (s.getStatus() == Settlement.SettlementStatus.COMPLETED) {
                Long payerId = s.getPayer().getId();
                Long receiverId = s.getReceiver().getId();
                settlementAdjustmentMap.put(payerId, settlementAdjustmentMap.getOrDefault(payerId, 0.0) + s.getAmount());
                settlementAdjustmentMap.put(receiverId, settlementAdjustmentMap.getOrDefault(receiverId, 0.0) - s.getAmount());
            }
        }

        List<MemberBalanceResponse> memberBalances = new ArrayList<>();
        Map<Long, String> userNameMap = new HashMap<>();
        Map<Long, Double> netBalanceMap = new HashMap<>();

        for (TripMember tm : members) {
            User u = tm.getUser();
            Long uid = u.getId();
            userNameMap.put(uid, u.getName());

            double totalPaid = paidMap.getOrDefault(uid, 0.0);
            double totalOwed = owedMap.getOrDefault(uid, 0.0);
            double settlementAdj = settlementAdjustmentMap.getOrDefault(uid, 0.0);
            double netBalance = round((totalPaid + settlementAdj) - totalOwed);

            netBalanceMap.put(uid, netBalance);

            memberBalances.add(MemberBalanceResponse.builder()
                    .userId(uid)
                    .name(u.getName())
                    .email(u.getEmail())
                    .profileImage(u.getProfileImage())
                    .totalPaid(round(totalPaid))
                    .totalOwed(round(totalOwed))
                    .balance(netBalance)
                    .build());
        }

        List<PaymentSuggestionResponse> paymentSuggestions = calculatePaymentSuggestions(netBalanceMap, userNameMap);

        return TripBalancesResponse.builder()
                .tripId(tripId)
                .totalExpenses(round(totalExpenses))
                .members(memberBalances)
                .settlementSuggestions(paymentSuggestions)
                .build();
    }

    private List<PaymentSuggestionResponse> calculatePaymentSuggestions(
            Map<Long, Double> netBalanceMap,
            Map<Long, String> userNameMap) {

        List<PaymentSuggestionResponse> suggestions = new ArrayList<>();

        class MemberBalance {
            Long userId;
            double amount;

            MemberBalance(Long userId, double amount) {
                this.userId = userId;
                this.amount = amount;
            }
        }

        List<MemberBalance> debtors = new ArrayList<>();
        List<MemberBalance> creditors = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : netBalanceMap.entrySet()) {
            double bal = entry.getValue();
            if (bal < -0.009) {
                debtors.add(new MemberBalance(entry.getKey(), -bal));
            } else if (bal > 0.009) {
                creditors.add(new MemberBalance(entry.getKey(), bal));
            }
        }

        int debtorIdx = 0;
        int creditorIdx = 0;

        while (debtorIdx < debtors.size() && creditorIdx < creditors.size()) {
            MemberBalance debtor = debtors.get(debtorIdx);
            MemberBalance creditor = creditors.get(creditorIdx);

            double settleAmount = Math.min(debtor.amount, creditor.amount);
            if (settleAmount > 0.009) {
                suggestions.add(PaymentSuggestionResponse.builder()
                        .payerId(debtor.userId)
                        .payer(userNameMap.get(debtor.userId))
                        .receiverId(creditor.userId)
                        .receiver(userNameMap.get(creditor.userId))
                        .amount(round(settleAmount))
                        .build());
            }

            debtor.amount -= settleAmount;
            creditor.amount -= settleAmount;

            if (debtor.amount < 0.01) {
                debtorIdx++;
            }
            if (creditor.amount < 0.01) {
                creditorIdx++;
            }
        }

        return suggestions;
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                sb.append(INVITE_CHARS.charAt(random.nextInt(INVITE_CHARS.length())));
            }
            code = sb.toString();
        } while (tripRepository.existsByInviteCode(code));
        return code;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private TripResponse mapToTripResponse(Trip trip, TripMember.MemberRole userRole, List<TripMemberResponse> members) {
        UserResponse createdBy = UserResponse.builder()
                .id(trip.getCreatedBy().getId())
                .name(trip.getCreatedBy().getName())
                .email(trip.getCreatedBy().getEmail())
                .profileImage(trip.getCreatedBy().getProfileImage())
                .build();

        return TripResponse.builder()
                .id(trip.getId())
                .name(trip.getName())
                .destination(trip.getDestination())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .budget(trip.getBudget())
                .inviteCode(trip.getInviteCode())
                .createdBy(createdBy)
                .memberCount(members != null ? members.size() : (trip.getMembers() != null ? trip.getMembers().size() : 0))
                .userRole(userRole)
                .members(members)
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    private TripMemberResponse mapToMemberResponse(TripMember member) {
        return TripMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .profileImage(member.getUser().getProfileImage())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
