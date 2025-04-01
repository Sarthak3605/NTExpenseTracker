package com.expenseTrackerProject.expensetracker.dto;

import lombok.Data;


@Data
public class BudgetRequest {
    private String departmentName;
    private Double totalBudget;
}
