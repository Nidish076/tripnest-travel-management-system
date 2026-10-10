package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.ExpenseRequest;
import com.tripnest.backend.dto.response.ExpenseParticipantResponse;
import com.tripnest.backend.dto.response.ExpenseResponse;
import com.tripnest.backend.dto.response.SettlementResponse;
import com.tripnest.backend.entity.*;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.*;
import com.tripnest.backend.security.GroupAuthorizationService;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.GroupExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupExpenseServiceImpl implements GroupExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupAuthorizationService authorizationService;

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    @Override
    @Transactional
    public ExpenseResponse createGroupExpense(Long groupId, Long tripId, ExpenseRequest request) {
        Long currentUserId = getCurrentUserId();
        authorizationService.requireMember(groupId, currentUserId);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (trip.getGroup() == null || !trip.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Trip is not associated with this group");
        }

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getPaidBy())) {
            throw new BadRequestException("Payer must be a member of the group");
        }

        User paidBy = userRepository.findById(request.getPaidBy()).orElseThrow();

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(Expense.ExpenseCategory.valueOf(request.getCategory().toUpperCase()))
                .expenseDate(request.getExpenseDate())
                .trip(trip)
                .paidBy(paidBy)
                .build();

        expense = expenseRepository.save(expense);

        List<ExpenseParticipant> participants = processParticipants(expense, groupId, request.getParticipants(), request.getAmount());
        participantRepository.saveAll(participants);

        return mapToResponse(expense, participants);
    }

    private List<ExpenseParticipant> processParticipants(Expense expense, Long groupId, List<Long> participantIds, Double amountDouble) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new BadRequestException("Expense must have at least one participant");
        }

        BigDecimal amount = BigDecimal.valueOf(amountDouble);
        BigDecimal numberOfParticipants = BigDecimal.valueOf(participantIds.size());
        
        BigDecimal baseShare = amount.divide(numberOfParticipants, 2, RoundingMode.DOWN);
        BigDecimal remainder = amount.subtract(baseShare.multiply(numberOfParticipants));

        List<ExpenseParticipant> participants = new ArrayList<>();
        for (int i = 0; i < participantIds.size(); i++) {
            Long userId = participantIds.get(i);
            
            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
                throw new BadRequestException("Participant " + userId + " is not a member of the group");
            }
            
            User user = userRepository.findById(userId).orElseThrow();
            BigDecimal share = baseShare;
            
            if (i == 0) { // Add remainder to the first person's share to ensure exact sum
                share = share.add(remainder);
            }
            
            participants.add(ExpenseParticipant.builder()
                    .expense(expense)
                    .user(user)
                    .shareAmount(share)
                    .settled(false)
                    .build());
        }
        return participants;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(Long groupId) {
        authorizationService.requireMember(groupId, getCurrentUserId());
        
        List<Expense> expenses = expenseRepository.findByTripGroupId(groupId);
        return expenses.stream()
                .map(expense -> {
                    List<ExpenseParticipant> parts = participantRepository.findByExpenseId(expense.getId());
                    return mapToResponse(expense, parts);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getGroupExpenseDetails(Long groupId, Long expenseId) {
        authorizationService.requireMember(groupId, getCurrentUserId());
        
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
                
        if (expense.getTrip().getGroup() == null || !expense.getTrip().getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Expense does not belong to this group");
        }
        
        List<ExpenseParticipant> parts = participantRepository.findByExpenseId(expenseId);
        return mapToResponse(expense, parts);
    }

    @Override
    @Transactional
    public ExpenseResponse updateGroupExpense(Long groupId, Long expenseId, ExpenseRequest request) {
        Long currentUserId = getCurrentUserId();
        
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
                
        if (expense.getTrip().getGroup() == null || !expense.getTrip().getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Expense does not belong to this group");
        }
        
        boolean isPayer = expense.getPaidBy().getId().equals(currentUserId);
        boolean isAdmin = authorizationService.isGroupAdmin(groupId, currentUserId);
        
        if (!isPayer && !isAdmin) {
            throw new UnauthorizedException("Only the payer or group admin can modify this expense");
        }
        
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(Expense.ExpenseCategory.valueOf(request.getCategory().toUpperCase()));
        expense.setExpenseDate(request.getExpenseDate());
        
        if (!expense.getPaidBy().getId().equals(request.getPaidBy())) {
            User newPayer = userRepository.findById(request.getPaidBy())
                    .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));
            expense.setPaidBy(newPayer);
        }
        
        expense = expenseRepository.save(expense);
        
        participantRepository.deleteByExpenseId(expenseId);
        List<ExpenseParticipant> parts = processParticipants(expense, groupId, request.getParticipants(), request.getAmount());
        participantRepository.saveAll(parts);
        
        return mapToResponse(expense, parts);
    }

    @Override
    @Transactional
    public void deleteGroupExpense(Long groupId, Long expenseId) {
        Long currentUserId = getCurrentUserId();
        
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));
                
        if (expense.getTrip().getGroup() == null || !expense.getTrip().getGroup().getId().equals(groupId)) {
            throw new BadRequestException("Expense does not belong to this group");
        }
        
        boolean isPayer = expense.getPaidBy().getId().equals(currentUserId);
        boolean isAdmin = authorizationService.isGroupAdmin(groupId, currentUserId);
        
        if (!isPayer && !isAdmin) {
            throw new UnauthorizedException("Only the payer or group admin can delete this expense");
        }
        
        participantRepository.deleteByExpenseId(expenseId);
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public SettlementResponse getExpenseSummary(Long groupId) {
        Long currentUserId = getCurrentUserId();
        authorizationService.requireMember(groupId, currentUserId);
        
        List<Expense> expenses = expenseRepository.findByTripGroupId(groupId);
        List<ExpenseParticipant> allParticipants = participantRepository.findByExpenseTripGroupId(groupId);
        
        BigDecimal totalExpenses = BigDecimal.ZERO;
        Map<Long, BigDecimal> paidMap = new HashMap<>();
        Map<Long, BigDecimal> shareMap = new HashMap<>();
        Map<Long, String> nameMap = new HashMap<>();
        
        // Initialize for all members
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        for (GroupMember m : members) {
            Long uid = m.getUser().getId();
            paidMap.put(uid, BigDecimal.ZERO);
            shareMap.put(uid, BigDecimal.ZERO);
            nameMap.put(uid, m.getUser().getName());
        }
        
        for (Expense e : expenses) {
            BigDecimal amt = BigDecimal.valueOf(e.getAmount());
            totalExpenses = totalExpenses.add(amt);
            
            Long payerId = e.getPaidBy().getId();
            paidMap.put(payerId, paidMap.getOrDefault(payerId, BigDecimal.ZERO).add(amt));
        }
        
        for (ExpenseParticipant p : allParticipants) {
            Long uid = p.getUser().getId();
            shareMap.put(uid, shareMap.getOrDefault(uid, BigDecimal.ZERO).add(p.getShareAmount()));
        }
        
        List<SettlementResponse.MemberSummary> summaries = new ArrayList<>();
        for (Long uid : nameMap.keySet()) {
            BigDecimal paid = paidMap.get(uid);
            BigDecimal share = shareMap.get(uid);
            BigDecimal balance = paid.subtract(share);
            
            summaries.add(SettlementResponse.MemberSummary.builder()
                    .userId(uid)
                    .userName(nameMap.get(uid))
                    .totalPaid(paid)
                    .totalShare(share)
                    .balance(balance)
                    .build());
        }
        
        BigDecimal yourPaid = paidMap.getOrDefault(currentUserId, BigDecimal.ZERO);
        BigDecimal yourShare = shareMap.getOrDefault(currentUserId, BigDecimal.ZERO);
        
        return SettlementResponse.builder()
                .totalExpenses(totalExpenses)
                .yourPaidAmount(yourPaid)
                .yourShare(yourShare)
                .yourBalance(yourPaid.subtract(yourShare))
                .memberSummaries(summaries)
                .build();
    }

    private ExpenseResponse mapToResponse(Expense expense, List<ExpenseParticipant> participants) {
        List<ExpenseParticipantResponse> partResponses = participants.stream()
                .map(p -> ExpenseParticipantResponse.builder()
                        .userId(p.getUser().getId())
                        .userName(p.getUser().getName())
                        .shareAmount(p.getShareAmount())
                        .settled(p.isSettled())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory().name())
                .expenseDate(expense.getExpenseDate())
                .paidById(expense.getPaidBy().getId())
                .paidByName(expense.getPaidBy().getName())
                .createdAt(expense.getCreatedAt())
                .participants(partResponses)
                .build();
    }
}
