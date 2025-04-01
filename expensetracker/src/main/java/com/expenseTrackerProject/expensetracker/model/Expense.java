package com.expenseTrackerProject.expensetracker.model;

import jakarta.persistence.*;
import lombok.*; //reduce use of getters setters etc

import java.time.LocalDate; //import local date

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity //mark as entity
@Table(name="expense")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String expenseName;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseType expenseType;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The employee who submitted the expense
	@ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
	@JsonIgnore //ignore the json
    private Department department;

 //lastly creted
	private String createdBy;

}
