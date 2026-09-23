package com.tripnest.backend.repository;

import com.tripnest.backend.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    List<ExpenseSplit> findByExpenseId(Long expenseId);

    List<ExpenseSplit> findByExpenseTripId(Long tripId);

    void deleteByExpenseId(Long expenseId);
}
