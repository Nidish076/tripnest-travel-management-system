package com.tripnest.backend.controller;

import com.tripnest.backend.dto.ApiResponse;
import com.tripnest.backend.dto.ExpenseRequest;
import com.tripnest.backend.dto.ExpenseResponse;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/api/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<ExpenseResponse>> addExpense(
            @PathVariable Long tripId,
            @Valid @RequestBody ExpenseRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        ExpenseResponse response = expenseService.addExpense(tripId, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense added successfully", response));
    }

    @GetMapping("/api/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getTripExpenses(@PathVariable Long tripId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        List<ExpenseResponse> response = expenseService.getTripExpenses(tripId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Trip expenses retrieved successfully", response));
    }

    @GetMapping("/api/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable Long expenseId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        ExpenseResponse response = expenseService.getExpenseById(expenseId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Expense retrieved successfully", response));
    }

    @PutMapping("/api/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        ExpenseResponse response = expenseService.updateExpense(expenseId, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    @DeleteMapping("/api/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long expenseId) {
        Long currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        expenseService.deleteExpense(expenseId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully"));
    }
}
