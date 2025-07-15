package com.expenseTrackerProject.expensetracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.model.Expense;
import com.expenseTrackerProject.expensetracker.model.ExpenseStatus;
import com.expenseTrackerProject.expensetracker.model.User;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    //Find expenses by status (for finance team)
    List<Expense> findByStatus(ExpenseStatus status);

    //Find pending expenses for a manager's department
    List<Expense> findByStatusAndUser_Department(ExpenseStatus status, Department department);

	@Query("SELECT e FROM Expense e WHERE e.status = 'PENDING' AND e.user.department = :department") //department specific
   List<Expense> findPendingExpensesByDepartment(@Param("department") Department department);

   @Query("SELECT e FROM Expense e WHERE e.status = 'APPROVED'")
   List<Expense> findApprovedExpenses();



    //Find all expenses by user
    List<Expense> findByUser(User user);
}
