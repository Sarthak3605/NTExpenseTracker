package com.expenseTrackerProject.expensetracker.controller;

import com.expenseTrackerProject.expensetracker.model.Expense;
import com.expenseTrackerProject.expensetracker.model.User;
import com.expenseTrackerProject.expensetracker.service.ExpenseService;
import com.expenseTrackerProject.expensetracker.service.UserService;
//import com.expenseTrackerProject.expensetracker.service.DepartmentService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final ExpenseService expenseService;
    private final UserService userService;
	//private final DepartmentService departmentService;


    @GetMapping("/employee")
    public ResponseEntity<List<Expense>> getEmployeeDashboard() {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(expenseService.getExpensesByUser(user.getId()));
    }

    @GetMapping("/manager")
    public ResponseEntity<List<Expense>> getManagerDashboard() {
        return ResponseEntity.ok(expenseService.getExpensesForManager());
    }

    @GetMapping("/finance")
    public ResponseEntity<List<Expense>> getFinanceDashboard() {
        return ResponseEntity.ok(expenseService.getApprovedExpensesForFinance());
    }

	/*   @PreAuthorize("hasRole('FINANCE_TEAM')")
    @PostMapping("/add")
    public ResponseEntity<?> addDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(departmentService.addDepartment(department));
    }

    @PreAuthorize("hasRole('FINANCE_TEAM')")
    @GetMapping("/all")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }*/
}
