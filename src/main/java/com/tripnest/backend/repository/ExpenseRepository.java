package com.tripnest.backend.repository;

import com.tripnest.backend.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByTripIdOrderByExpenseDateDescCreatedAtDesc(Long tripId);

    List<Expense> findByTripId(Long tripId);
}