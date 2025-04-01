package com.expenseTrackerProject.expensetracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {
    @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @ManyToOne
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense; // The expense being approved

     @ManyToOne
    @JoinColumn(name = "approved_by", nullable = false)
    private User manager; // The manager who approves/rejects

    @Enumerated(EnumType.STRING) //use for enums in this
    @Column(nullable = false)
     private ExpenseStatus status; // Approved, Rejected

    private String comment; // Optional: Reason for approval/rejection

    @Column(nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
     private LocalDateTime approvalDate; // Timestamp when approval was made

    @PrePersist
    protected void onCreate() {
        approvalDate = LocalDateTime.now();
    }
}
