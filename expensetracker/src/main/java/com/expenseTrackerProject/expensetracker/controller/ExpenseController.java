package com.expenseTrackerProject.expensetracker.controller;

import com.expenseTrackerProject.expensetracker.dto.ExpenseRequest;
import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.model.Expense;
import com.expenseTrackerProject.expensetracker.model.ExpenseStatus;
import com.expenseTrackerProject.expensetracker.model.User;
import com.expenseTrackerProject.expensetracker.service.ExpenseService;
import com.expenseTrackerProject.expensetracker.repository.ExpenseRepository;
import com.expenseTrackerProject.expensetracker.repository.DepartmentRepository;
import com.expenseTrackerProject.expensetracker.service.UserService;
import com.expenseTrackerProject.expensetracker.security.JwtUtil;

import org.springframework.security.core.context.SecurityContextHolder;

import lombok.extern.slf4j.Slf4j; //logger
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.expenseTrackerProject.expensetracker.repository.UserRepository;

@Slf4j
@CrossOrigin(origins = "http://127.0.0.1:5500") //link the frontend part here
@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;
	private final UserService userService;
	private final JwtUtil jwtUtil;
	private final ExpenseRepository expenseRepository;
	private final DepartmentRepository departmentRepository;
	private final UserRepository userRepository;

//to add expenses
     	@PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/add")
     public ResponseEntity<?> addExpense(@RequestBody ExpenseRequest request) {

        User user = getAuthenticatedUser(); //authenticate user first
        if (user == null) {
            log.warn("Unauthorized attempt to add expense");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        log.info("Authenticated User: {}", user.getEmail()); // Log user email

        try {
            Expense savedExpense = expenseService.addExpense(request,user);
            log.info("Expense saved successfully: {}", savedExpense);
            return ResponseEntity.ok(savedExpense);
        } catch (Exception e) {
            log.error("Error saving expense: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Expense could not be saved.");
        }
    }

//to get all expenses and see them
  @GetMapping("/my-expenses")
public ResponseEntity<List<Expense>> getExpensesForUser() {
    User user = getAuthenticatedUser();
    List<Expense> expenses = expenseService.getExpensesForUser(user);

    if (expenses.isEmpty()) {
        return ResponseEntity.ok(new ArrayList<>()); // Return an empty array if no expenses
    }
    return ResponseEntity.ok(expenses);
}

//delete the expenses before the approval
    @PreAuthorize("hasRole('EMPLOYEE')")
    @DeleteMapping("/delete/{expenseId}")
    public ResponseEntity<String> deleteExpenseBeforeApproval(@PathVariable Long expenseId,@RequestHeader("Authorization") String token) {
		String jwt = token.replace("Bearer ", "");
		List<String> roles = jwtUtil.extractRoles(jwt);

		if (!roles.contains("EMPLOYEE")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Employees can delete expenses.");
		}

		Optional<Expense> expenseOpt = expenseRepository.findById(expenseId);

        if (expenseOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found!");
        }

        Expense expense = expenseOpt.get();
		System.out.println("Deleting Expense : "+expenseId+", Status "+expense.getStatus());

		if (!ExpenseStatus.PENDING.equals(expense.getStatus())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only pending expenses can be deleted!");
		}

	    expenseService.deleteExpenseBeforeApproval(expenseId);
        return ResponseEntity.ok("Expense deleted successfully");
    }

//get all pending approvals list
    @GetMapping("/pending-approvals")
    public ResponseEntity<List<Expense>> getPendingApprovals() {
        return ResponseEntity.ok(expenseService.getExpensesForManager());
    }

//get all pending expenses for manager dashboard
	@GetMapping("/pending")
	@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<List<Expense>> getPendingExpensesForManager() {
	User manager = getAuthenticatedUser();
	if (manager == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

	List<Expense> pendingExpenses = expenseService.getPendingExpensesForManager();
    return ResponseEntity.ok(pendingExpenses);
}

//to update the status by manager either approve or reject
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{expenseId}/update-status")
    public ResponseEntity<?> updateExpenseStatus(@PathVariable Long expenseId, @RequestParam ExpenseStatus status) {
		User manager = getAuthenticatedUser();
		if (manager == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
		}
		log.info("Authenticated Manager: {}, Department: {}", manager.getEmail(), manager.getDepartment());

        Expense expense = expenseService.getExpenseById(expenseId);
		if (expense == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found");
		}

		log.info("Expense ID: {}, Requested by User: {}, Department: {}",
		expense.getId(), expense.getUser().getEmail(), expense.getUser().getDepartment());

		//Ensure manager can only approve/reject expenses from their own department
		if (!manager.getDepartment().getId().equals(expense.getDepartment().getId())) {
            log.warn("Manager or Expense user department is null");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Department data is missing!");
        }


		if (!manager.getDepartment().getId().equals(expense.getDepartment().getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only manage expenses from your department!");
		}

		try {
			ExpenseStatus expenseStatus = status;
			Expense updatedExpense = expenseService.updateExpenseStatus(expenseId, expenseStatus);
			log.info("Expense {} successfully updated to {}", expenseId, status);
			return ResponseEntity.ok(updatedExpense);
		} catch (IllegalArgumentException e) {
			log.error("Invalid status value: {}", status);
			return ResponseEntity.badRequest().body(null); // this Handle invalid status
		}
    }

	//approval
    @GetMapping("/finance/approved-expenses")
    public ResponseEntity<List<Expense>> getApprovedExpensesForFinance() {
        return ResponseEntity.ok(expenseService.getApprovedExpensesForFinance());
    }

	//final approval by finance team to mark as paid
	@PreAuthorize("hasAnyAuthority('FINANCE_TEAM','ROLE_FINANCE_TEAM')")
@PutMapping("/{expenseId}/mark-paid")
public ResponseEntity<?> markExpenseAsPaid(@PathVariable Long expenseId) {
	log.info("Finance user attempting to mark expense {} as paid", expenseId);

    User financeUser = getAuthenticatedUser();
    log.info("Authenticated Finance User: {}", financeUser.getEmail());
    log.info("User Role: {}", financeUser.getRole());

    Expense expense = expenseService.getExpenseById(expenseId);
    if (expense == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found!");
    }

    try {
        Expense updatedExpense = expenseService.markExpenseAsPaid(expenseId);
        return ResponseEntity.ok(updatedExpense);
    } catch (RuntimeException e) {
        log.error("Failed to mark expense as paid: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to mark as paid: " + e.getMessage());
    }
}

//see all pending payments
	@PreAuthorize("hasRole('FINANCE')")
	@GetMapping("/finance/pending-payments")
	public ResponseEntity<List<Expense>> getPendingPaymentsForFinance() {
		List<Expense> pendingPayments = expenseService.getPendingPayments();
		if (pendingPayments.isEmpty()) {
			return ResponseEntity.ok(new ArrayList<>()); // Return an empty array instead of null
		}
		return ResponseEntity.ok(pendingPayments);
	}

	//authentication function
	 private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            // Assuming you have a method to find a user by email
            return expenseService.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        } else {
            throw new RuntimeException("No authenticated user found");
        }
    }
//add expense for managers to add their expenses

	@PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/manager/add")
    public ResponseEntity<String> addExpenseByManager(@RequestBody Expense expense, @RequestHeader("Authorization") String token) {
		try{
        String jwt = token.replace("Bearer ", "");
        String managerEmail = jwtUtil.extractEmail(jwt);
		String departmentName = jwtUtil.extractDepartment(jwt);

		Optional<User> userOptional = userRepository.findByEmail(managerEmail);
		if (userOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID is missing! Login again.");
		}

		User user = userOptional.get(); //Correctly get the user
		Department departmentEntity = departmentRepository.findByName(departmentName);
        // Check if the department is valid
        if (departmentEntity == null) {
			return ResponseEntity.badRequest().body("Invalid department." + departmentName);
        }

        expense.setCreatedBy(managerEmail);
		expense.setUser(user);
        expense.setDepartment(departmentEntity);

		expense.setStatus(ExpenseStatus.PENDING);
        expenseService.addExpenseByManager(expense);
        return ResponseEntity.ok("Expense added successfully and awaiting approval.");
	}
	catch(Exception e){
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding expense.");
	}

    }

	//approve the expense by finance team of managers
	@PreAuthorize("hasRole('FINANCE_TEAM')")
	@PutMapping("/approve/{expenseId}")
	public ResponseEntity<String> approveExpense(@PathVariable Long expenseId) {
		Optional<Expense> expenseOpt = expenseRepository.findById(expenseId);

		if (expenseOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found!");
		}

		Expense expense = expenseOpt.get();
		expense.setStatus(ExpenseStatus.APPROVED);
		expenseRepository.save(expense);

		return ResponseEntity.ok("Expense approved successfully!");
	}

	//reject the expense
	@PreAuthorize("hasRole('FINANCE_TEAM')")
	@PutMapping("/reject/{expenseId}")
	public ResponseEntity<String> rejectExpense(@PathVariable Long expenseId) {
		Optional<Expense> expenseOpt = expenseRepository.findById(expenseId);

		if (expenseOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Expense not found!");
		}

		Expense expense = expenseOpt.get();
		expense.setStatus(ExpenseStatus.REJECTED);
		expenseRepository.save(expense);

		return ResponseEntity.ok("Expense rejected!");
	}
}