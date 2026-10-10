package com.tripnest.backend.controller;

import com.tripnest.backend.dto.request.ExpenseRequest;
import com.tripnest.backend.dto.response.ApiResponse;
import com.tripnest.backend.dto.response.ExpenseResponse;
import com.tripnest.backend.dto.response.SettlementResponse;
import com.tripnest.backend.service.GroupExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class GroupExpenseController {

    private final GroupExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @PathVariable Long groupId,
            @RequestParam Long tripId,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created", expenseService.createGroupExpense(groupId, tripId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpenses(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getGroupExpenses(groupId)));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(
            @PathVariable Long groupId, 
            @PathVariable Long expenseId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getGroupExpenseDetails(groupId, expenseId)));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long groupId, 
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Expense updated", expenseService.updateGroupExpense(groupId, expenseId, request)));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long groupId, 
            @PathVariable Long expenseId) {
        expenseService.deleteGroupExpense(groupId, expenseId);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted", null));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SettlementResponse>> getSummary(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpenseSummary(groupId)));
    }
}
