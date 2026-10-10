package com.tripnest.backend.repository;

import com.tripnest.backend.entity.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {
    List<ExpenseParticipant> findByExpenseId(Long expenseId);
    List<ExpenseParticipant> findByExpenseTripGroupId(Long groupId);
    void deleteByExpenseId(Long expenseId);
}
