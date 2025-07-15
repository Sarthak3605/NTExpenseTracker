package com.expenseTrackerProject.expensetracker.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expenseTrackerProject.expensetracker.model.Approval;
import com.expenseTrackerProject.expensetracker.model.Expense;
import com.expenseTrackerProject.expensetracker.model.ExpenseStatus;
import com.expenseTrackerProject.expensetracker.model.User;
import com.expenseTrackerProject.expensetracker.repository.ApprovalRepository;
import com.expenseTrackerProject.expensetracker.repository.ExpenseRepository;
import com.expenseTrackerProject.expensetracker.repository.UserRepository;

@Service
public class ApprovalService {
    @Autowired //dependency injection to connect
    private ApprovalRepository approvalRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    public Approval approveExpense(Long expenseId, Long managerId, String comment) {
        Optional<Expense> expenseOpt = expenseRepository.findById(expenseId);
        Optional<User> managerOpt = userRepository.findById(managerId);

        if (expenseOpt.isPresent() && managerOpt.isPresent()) {
            Expense expense = expenseOpt.get();
            User manager = managerOpt.get();


            Approval approval = Approval.builder()
                .expense(expense)
                .manager(manager)
                .status(ExpenseStatus.APPROVED)
                .comment(comment)
                .build();

            return approvalRepository.save(approval);
        }
        throw new RuntimeException("Expense or Manager not found!");
    }

    public Approval rejectExpense(Long expenseId, Long managerId, String comment) {
        Optional<Expense> expenseOpt = expenseRepository.findById(expenseId); //use optional for single result and avoid null pointer
        Optional<User> managerOpt = userRepository.findById(managerId);

        if (expenseOpt.isPresent() && managerOpt.isPresent()) {
            Expense expense = expenseOpt.get();
            User manager = managerOpt.get();

            Approval approval = Approval.builder()
                .expense(expense)
                .manager(manager)
                .status(ExpenseStatus.REJECTED)
                .comment(comment)
                .build();

            return approvalRepository.save(approval);
        }
        throw new RuntimeException("Expense or Manager not found!");
    }
}
