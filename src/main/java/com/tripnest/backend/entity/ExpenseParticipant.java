package com.tripnest.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_participants",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"expense_id", "user_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "share_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal shareAmount;

    @Column(nullable = false)
    private boolean settled = false;
}
