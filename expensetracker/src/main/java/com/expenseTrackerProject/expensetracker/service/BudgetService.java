package com.expenseTrackerProject.expensetracker.service;

import com.expenseTrackerProject.expensetracker.dto.BudgetRequest;
import com.expenseTrackerProject.expensetracker.model.Budget;
import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.repository.BudgetRepository;
import com.expenseTrackerProject.expensetracker.repository.DepartmentRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final DepartmentRepository departmentRepository;

	//set budget for departments and controllers will call this and these service calls repository

    @PreAuthorize("hasRole('FINANCE_TEAM')") //Fixed security annotation
    public Budget setBudget(Budget budget) {
        budget.setRemainingBudget(budget.getTotalBudget()); // Initially, remaining budget = total budget
		        if (budget.getStartDate() == null) {
            budget.setStartDate(LocalDate.now()); // Default to today
        }
        if (budget.getEndDate() == null) {
            budget.setEndDate(budget.getStartDate().plusMonths(1)); // Default to 1 month later
        }

        return budgetRepository.save(budget);
    }

    public Optional<Budget> getBudgetForDepartment(String departmentName) {
        Department department = departmentRepository.findByNameIgnoreCase(departmentName)
            .orElseThrow(() -> new RuntimeException("Department not found: " + departmentName)); //string concatenation

        return budgetRepository.findLatestBudgetByDepartment(department);
    }

	@PreAuthorize("hasRole('FINANCE_TEAM')")
    public Budget allocateBudget(BudgetRequest request) {
        Department department = departmentRepository.findByNameIgnoreCase(request.getDepartmentName()) //finds the department
                .orElseThrow(() -> new RuntimeException("Department not found: " + request.getDepartmentName()));

				boolean budgetExists = budgetRepository.findLatestBudgetByDepartment(department).isPresent();

					if(budgetExists){
					throw new RuntimeException("Budget for this department already exists!");
					}


				Budget budget = Budget.builder()
				.department(department)
				.totalBudget(request.getTotalBudget())
				.remainingBudget(request.getTotalBudget())
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusMonths(1))
				.build();

        return budgetRepository.save(budget);
    }

	//get remaining budget each one is mapped with the remaining with department

	public Double getRemainingBudget(String departmentName) {
		Department department = departmentRepository.findByNameIgnoreCase(departmentName)
			.orElseThrow(() -> new RuntimeException("Department not found: " + departmentName));

		return budgetRepository.findLatestBudgetByDepartment(department)
			.map(Budget::getRemainingBudget)
			.orElseThrow(() -> new RuntimeException("No budget set for department: " + departmentName));
	}

//update budget to ui
    public void updateRemainingBudget(Department department, Double amountSpent) { //Fixed parameter name
        Budget budget = budgetRepository.findLatestBudgetByDepartment(department)
                .orElseThrow(() -> new RuntimeException("Budget not found for department: " + department.getName())); //Fixed string concatenation

        if (budget.getRemainingBudget() >= amountSpent) {
            budget.setRemainingBudget(budget.getRemainingBudget() - amountSpent);
            budgetRepository.save(budget);
            System.out.println("Updating remaining budget for department " + department.getName() +
                    ". New remaining budget: " + budget.getRemainingBudget()); //Improved log readability
        } else {
            throw new RuntimeException("Budget exceeded for department: " + department.getName()); //error message
        }
    }
}
