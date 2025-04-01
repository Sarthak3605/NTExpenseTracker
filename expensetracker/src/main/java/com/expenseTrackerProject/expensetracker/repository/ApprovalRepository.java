package com.expenseTrackerProject.expensetracker.repository;

import com.expenseTrackerProject.expensetracker.model.Approval;
import com.expenseTrackerProject.expensetracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByExpense(Expense expense);
}
