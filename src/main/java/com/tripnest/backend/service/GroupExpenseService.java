package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.ExpenseRequest;
import com.tripnest.backend.dto.response.ExpenseResponse;
import com.tripnest.backend.dto.response.SettlementResponse;

import java.util.List;

public interface GroupExpenseService {
    ExpenseResponse createGroupExpense(Long groupId, Long tripId, ExpenseRequest request);
    List<ExpenseResponse> getGroupExpenses(Long groupId);
    ExpenseResponse getGroupExpenseDetails(Long groupId, Long expenseId);
    ExpenseResponse updateGroupExpense(Long groupId, Long expenseId, ExpenseRequest request);
    void deleteGroupExpense(Long groupId, Long expenseId);
    SettlementResponse getExpenseSummary(Long groupId);
}
