package com.expenseTrackerProject.expensetracker.controller;

import com.expenseTrackerProject.expensetracker.model.Budget;
import com.expenseTrackerProject.expensetracker.service.BudgetService;
import lombok.RequiredArgsConstructor;

import com.expenseTrackerProject.expensetracker.repository.UserRepository;
import com.expenseTrackerProject.expensetracker.model.User;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/budget") //make request at budget endpoint
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;
	private final UserRepository userRepository;

    @PreAuthorize("hasRole('FINANCE_TEAM')") //only access by finance team members
    @PostMapping("/set") //to set budget
    public ResponseEntity<?> setBudget(@RequestBody Budget budget) {
		try{
			User user = getAuthenticatedUser();
			if(!user.getRole().name().equals("FINANCE_TEAM")){
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied!");
			}

        Budget savedBudget = budgetService.setBudget(budget);
        return ResponseEntity.ok(savedBudget);
		}catch(Exception e){
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: "+e.getMessage());
		}
    }

    @GetMapping("/{department}")
    public ResponseEntity<Budget> getBudget(@PathVariable("department") String departmentName) {
        Optional<Budget> budget = budgetService.getBudgetForDepartment(departmentName);

        // Check if the Optional is present and return the Budget, or return 404
		return budget.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
	    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal(); //Spring Security authentication brings current logged in user

        if (principal instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();  //Get authenticated user's email

            //Fetch user from the database using email
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found in database."));
        }

        throw new RuntimeException("No authenticated user found.");
    }
    @PreAuthorize("hasRole('FINANCE_TEAM')")
	@GetMapping("/remaining/{department}")
    public ResponseEntity<Double> getRemainingBudget(@PathVariable String department) {
    try {
        Double remainingBudget = budgetService.getRemainingBudget(department);
        return ResponseEntity.ok(remainingBudget);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
}
