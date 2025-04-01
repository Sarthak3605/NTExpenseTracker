package com.expenseTrackerProject.expensetracker.dto;

import com.expenseTrackerProject.expensetracker.model.ExpenseType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExpenseRequest {
    private String expenseName;
    private Double amount;
    private ExpenseType expenseType;
    private LocalDate expenseDate;
}
