package com.expenseTrackerProject.expensetracker.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expenseTrackerProject.expensetracker.dto.ExpenseRequest;
import com.expenseTrackerProject.expensetracker.model.Budget;
import com.expenseTrackerProject.expensetracker.model.Expense;
import com.expenseTrackerProject.expensetracker.model.ExpenseStatus;
import com.expenseTrackerProject.expensetracker.model.ExpenseType;
import com.expenseTrackerProject.expensetracker.model.User;
import com.expenseTrackerProject.expensetracker.repository.BudgetRepository;
import com.expenseTrackerProject.expensetracker.repository.ExpenseRepository;
import com.expenseTrackerProject.expensetracker.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ExpenseService {

	private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository; //Added BudgetRepository

	//add new expense by employee
   public Expense addExpense(ExpenseRequest request, User user) {
    if (request.getExpenseType() == null) {
        throw new RuntimeException("Expense Type cannot be null!");
    }

    try {
        String expenseTypeString = request.getExpenseType().toString().toUpperCase();
        request.setExpenseType(ExpenseType.valueOf(expenseTypeString));
    } catch (IllegalArgumentException e) {
        logger.error("Invalid Expense Type: {}", request.getExpenseType());
        throw new RuntimeException("Invalid Expense Type: " + request.getExpenseType());
    }

    if (user.getDepartment() == null) {
        logger.error("User {} does not belong to any department", user.getEmail());
        throw new RuntimeException("User does not belong to any department");
    }

    Long departmentId = user.getDepartment().getId();
    Budget budget = budgetRepository.findByDepartment_Id(departmentId)
	.orElseThrow(() -> new RuntimeException("No budget found for department id: " + departmentId));

    logger.info("Found budget for department: {} with remaining budget: {}",
            user.getDepartment().getName(), budget.getRemainingBudget());

    if (budget.getRemainingBudget() < request.getAmount()) {
        throw new RuntimeException("Insufficient budget for expense");
    }

    budget.setRemainingBudget(budget.getRemainingBudget() - request.getAmount());
    budgetRepository.save(budget);

	//create new expense with it's details
    Expense expense = new Expense();
    expense.setExpenseName(request.getExpenseName());
    expense.setAmount(request.getAmount());
    expense.setExpenseType(request.getExpenseType());
    expense.setExpenseDate(request.getExpenseDate());
    expense.setStatus(ExpenseStatus.PENDING); //Pending is default
    expense.setUser(user);
    expense.setDepartment(user.getDepartment());

    logger.info("Expense added successfully: {}", expense); //very useful for debug,error,log...etc
    return expenseRepository.save(expense);
}

//get expenses and payments

    public List<Expense> getExpensesForUser(User user) {
        return expenseRepository.findByUser(user);
    }
	public List<Expense> getPendingPayments() {
		return expenseRepository.findByStatus(ExpenseStatus.APPROVED);
	}
	//delete the expense before approval here this is called by controller and this calls repository

    public void deleteExpenseBeforeApproval(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }

//authenticate the user any
    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        throw new RuntimeException("No authenticated user found");
    }

	//get all expenses for managers
    public List<Expense> getExpensesForManager() {
        User manager = getAuthenticatedUser();

        // Fetch expenses that are pending approval and belong to the manager's department
        return expenseRepository.findByStatusAndUser_Department(ExpenseStatus.PENDING, manager.getDepartment());
    }
	public Expense getExpenseById(Long expenseId) {
		return expenseRepository.findById(expenseId)
			.orElse(null); // Returns null if expense not found
	}

	//updates the sstatus to approve or reject from department
    public Expense updateExpenseStatus(Long expenseId, ExpenseStatus status) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        User manager = getAuthenticatedUser();

        // Ensure only the manager of the same department can approve/reject --added feature
		if (!manager.getDepartment().getId().equals(expense.getUser().getDepartment().getId())) {
			throw new RuntimeException("You can only approve/reject expenses from your own department");
		}

        expense.setStatus(status);
        return expenseRepository.save(expense);
    }
// get approve expenses
    public List<Expense> getApprovedExpensesForFinance() {
        return expenseRepository.findByStatus(ExpenseStatus.APPROVED);
    }

	//get expenses for manager
	public List<Expense> getPendingExpensesForManager() {
		User manager = getAuthenticatedUser();
		if(manager.getDepartment() == null){
			throw new RuntimeException("Manager does not belong to any deparment!");
		}
		return expenseRepository.findPendingExpensesByDepartment(manager.getDepartment());
	}

//to get
    public List<Expense> getExpensesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return expenseRepository.findByUser(user);
    }
	//to find by email
	public Optional<User> findUserByEmail(String email){
		return userRepository.findByEmail(email);
	}
	//to approve
	public Expense approveExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setStatus(ExpenseStatus.APPROVED);
        return expenseRepository.save(expense);
    }
 //to reject the expense
    public Expense rejectExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setStatus(ExpenseStatus.REJECTED);

        // Refund the budget amount if rejected
        Budget budget = budgetRepository.findByDepartment_Id(expense.getDepartment().getId())
		.orElseThrow(() -> new RuntimeException("No budget found for this department"));


            budget.setRemainingBudget(budget.getRemainingBudget() + expense.getAmount());
            budgetRepository.save(budget);


        return expenseRepository.save(expense);
    }
	//get expenses
	public List<Expense> getPendingExpenses() {
        return expenseRepository.findByStatus(ExpenseStatus.PENDING);
    }
	//add expense by managers to finance team members
	    public Expense addExpenseByManager(Expense expense) {
        expense.setStatus(ExpenseStatus.PENDING); // Default status
        return expenseRepository.save(expense);
    }

	//mark as paid for finance team dashboard

   @Transactional
	public Expense markExpenseAsPaid(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

				logger.info("Current Expense Status: {}", expense.getStatus());
        if (ExpenseStatus.PAID.equals(expense.getStatus())) {
            throw new RuntimeException("Already marked as paid");
        }
		if (!ExpenseStatus.APPROVED.equals(expense.getStatus())) {
			throw new RuntimeException("Only approved expenses can be marked as paid");
		}
        User user = getAuthenticatedUser();

        Budget budget = budgetRepository.findLatestBudgetByDepartment(user.getDepartment())
                .orElseThrow(() -> {
                    logger.error("No budget found for department: {}", user.getDepartment().getName());
                    return new RuntimeException("No budget set for department: " + user.getDepartment().getName());
                });

        if (budget.getRemainingBudget() < expense.getAmount()) {
			logger.warn("Warning: Insufficient budget, but marking the expense as paid anyway.");
        }
		budget.setRemainingBudget(budget.getRemainingBudget() - expense.getAmount());
        budgetRepository.save(budget); // Update remaining budget

        expense.setStatus(ExpenseStatus.PAID);
        return expenseRepository.save(expense);
    }
}
