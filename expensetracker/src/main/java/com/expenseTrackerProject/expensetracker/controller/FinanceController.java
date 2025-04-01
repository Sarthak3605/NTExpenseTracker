package com.expenseTrackerProject.expensetracker.controller;

import java.util.List;
import com.expenseTrackerProject.expensetracker.service.BudgetService;
import com.expenseTrackerProject.expensetracker.service.ExpenseService;
import com.expenseTrackerProject.expensetracker.dto.BudgetRequest;
import com.expenseTrackerProject.expensetracker.model.Expense;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final BudgetService budgetService;
    private final ExpenseService expenseService;
	//allocation of budget for departments

	@PreAuthorize("hasAuthority('FINANCE_TEAM')")
	@PostMapping("/allocate")
	public ResponseEntity<?> allocateBudget(@RequestHeader("Authorization") String token,
											@RequestBody BudgetRequest request) {
		budgetService.allocateBudget(request);
		return ResponseEntity.ok("Budget allocated successfully.");
	}
//pending payments
@PreAuthorize("hasRole('FINANCE')")
@GetMapping("/pending-payments")
public ResponseEntity<List<Expense>> getPendingPayments() {
    List<Expense> pendingPayments = expenseService.getPendingPayments();
    return ResponseEntity.ok(pendingPayments);
}

}
