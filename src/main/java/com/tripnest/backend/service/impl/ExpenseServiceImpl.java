package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.*;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.model.*;
import com.tripnest.backend.repository.*;
import com.tripnest.backend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ExpenseResponse addExpense(Long tripId, ExpenseRequest request, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You must be a member of this trip to add expenses"));

        Long payerId = request.getPaidBy() != null ? request.getPaidBy() : currentUserId;
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payer user not found with id: " + payerId));

        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, payerId)) {
            throw new BadRequestException("Payer must be a member of this trip");
        }

        double totalAmount = round(request.getAmount());

        Expense expense = Expense.builder()
                .trip(trip)
                .title(request.getTitle().trim())
                .amount(totalAmount)
                .category(request.getCategory())
                .paidBy(payer)
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        List<ExpenseSplit> createdSplits = buildAndValidateSplits(tripId, savedExpense, totalAmount, request.getSplits());
        expenseSplitRepository.saveAll(createdSplits);
        savedExpense.setSplits(createdSplits);

        return mapToExpenseResponse(savedExpense, createdSplits);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getTripExpenses(Long tripId, Long currentUserId) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        List<Expense> expenses = expenseRepository.findByTripIdOrderByExpenseDateDescCreatedAtDesc(tripId);

        return expenses.stream().map(expense -> {
            List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expense.getId());
            return mapToExpenseResponse(expense, splits);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long expenseId, Long currentUserId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        tripMemberRepository.findByTripIdAndUserId(expense.getTrip().getId(), currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expense.getId());
        return mapToExpenseResponse(expense, splits);
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request, Long currentUserId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        Long tripId = expense.getTrip().getId();
        TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        boolean isPayer = expense.getPaidBy().getId().equals(currentUserId);
        boolean isPrivileged = member.getRole() == TripMember.MemberRole.OWNER || member.getRole() == TripMember.MemberRole.ADMIN;

        if (!isPayer && !isPrivileged) {
            throw new AccessDeniedException("You do not have permission to update this expense");
        }

        Long payerId = request.getPaidBy() != null ? request.getPaidBy() : expense.getPaidBy().getId();
        User payer = userRepository.findById(payerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payer user not found with id: " + payerId));

        if (!tripMemberRepository.existsByTripIdAndUserId(tripId, payerId)) {
            throw new BadRequestException("Payer must be a member of this trip");
        }

        double totalAmount = round(request.getAmount());

        expense.setTitle(request.getTitle().trim());
        expense.setAmount(totalAmount);
        expense.setCategory(request.getCategory());
        expense.setPaidBy(payer);
        expense.setDescription(request.getDescription());
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }

        expenseSplitRepository.deleteByExpenseId(expenseId);

        List<ExpenseSplit> updatedSplits = buildAndValidateSplits(tripId, expense, totalAmount, request.getSplits());
        expenseSplitRepository.saveAll(updatedSplits);
        expense.setSplits(updatedSplits);

        Expense updatedExpense = expenseRepository.save(expense);
        return mapToExpenseResponse(updatedExpense, updatedSplits);
    }

    @Override
    @Transactional
    public void deleteExpense(Long expenseId, Long currentUserId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        Long tripId = expense.getTrip().getId();
        TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        boolean isPayer = expense.getPaidBy().getId().equals(currentUserId);
        boolean isPrivileged = member.getRole() == TripMember.MemberRole.OWNER || member.getRole() == TripMember.MemberRole.ADMIN;

        if (!isPayer && !isPrivileged) {
            throw new AccessDeniedException("You do not have permission to delete this expense");
        }

        expenseSplitRepository.deleteByExpenseId(expenseId);
        expenseRepository.delete(expense);
    }

    private List<ExpenseSplit> buildAndValidateSplits(
            Long tripId,
            Expense expense,
            double totalAmount,
            List<ExpenseSplitRequest> splitRequests) {

        List<ExpenseSplit> splits = new ArrayList<>();

        if (splitRequests != null && !splitRequests.isEmpty()) {
            double sum = 0.0;
            for (ExpenseSplitRequest req : splitRequests) {
                if (!tripMemberRepository.existsByTripIdAndUserId(tripId, req.getUserId())) {
                    throw new BadRequestException("User " + req.getUserId() + " in expense split is not a member of this trip");
                }
                User splitUser = userRepository.findById(req.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Split user not found with id: " + req.getUserId()));

                double splitAmount = round(req.getAmount());
                sum += splitAmount;

                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .user(splitUser)
                        .amount(splitAmount)
                        .settled(false)
                        .build());
            }

            if (Math.abs(round(sum) - totalAmount) > 0.01) {
                throw new BadRequestException("Sum of split amounts (" + round(sum) + ") does not equal the expense total amount (" + totalAmount + ")");
            }
        } else {
            // Default: Equal split among all trip members
            List<TripMember> members = tripMemberRepository.findByTripId(tripId);
            if (members.isEmpty()) {
                throw new BadRequestException("No trip members found to split expense");
            }

            int n = members.size();
            double baseAmount = Math.floor((totalAmount / n) * 100.0) / 100.0;
            double remainder = round(totalAmount - (baseAmount * n));

            for (int i = 0; i < n; i++) {
                TripMember tm = members.get(i);
                double memberSplit = (i == 0) ? round(baseAmount + remainder) : baseAmount;

                splits.add(ExpenseSplit.builder()
                        .expense(expense)
                        .user(tm.getUser())
                        .amount(memberSplit)
                        .settled(false)
                        .build());
            }
        }

        return splits;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense, List<ExpenseSplit> splits) {
        UserResponse paidBy = UserResponse.builder()
                .id(expense.getPaidBy().getId())
                .name(expense.getPaidBy().getName())
                .email(expense.getPaidBy().getEmail())
                .profileImage(expense.getPaidBy().getProfileImage())
                .build();

        List<ExpenseSplitResponse> splitResponses = splits != null ? splits.stream().map(split ->
                ExpenseSplitResponse.builder()
                        .id(split.getId())
                        .userId(split.getUser().getId())
                        .userName(split.getUser().getName())
                        .userEmail(split.getUser().getEmail())
                        .amount(split.getAmount())
                        .settled(split.getSettled())
                        .build()
        ).collect(Collectors.toList()) : List.of();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .tripId(expense.getTrip().getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .paidBy(paidBy)
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .splits(splitResponses)
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
