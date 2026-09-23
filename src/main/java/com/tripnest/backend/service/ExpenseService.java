package com.tripnest.backend.service;

import com.tripnest.backend.dto.ExpenseRequest;
import com.tripnest.backend.dto.ExpenseResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse addExpense(Long tripId, ExpenseRequest request, Long currentUserId);
    List<ExpenseResponse> getTripExpenses(Long tripId, Long currentUserId);
    ExpenseResponse getExpenseById(Long expenseId, Long currentUserId);
    ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request, Long currentUserId);
    void deleteExpense(Long expenseId, Long currentUserId);
}
