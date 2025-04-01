package com.expenseTrackerProject.expensetracker.controller;

import com.expenseTrackerProject.expensetracker.model.Approval;
import com.expenseTrackerProject.expensetracker.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approvals") //it will run on this endpoint when we request to /endpoint
public class ApprovalController {
    @Autowired //dependency injection
    private ApprovalService approvalService;

    @PostMapping("/approve/{expenseId}/{managerId}")
    public ResponseEntity<Approval> approveExpense(
            @PathVariable Long expenseId, //defines the path
            @PathVariable Long managerId,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(approvalService.approveExpense(expenseId, managerId, comment));
    }

    @PostMapping("/reject/{expenseId}/{managerId}")
    public ResponseEntity<Approval> rejectExpense(
            @PathVariable Long expenseId,
            @PathVariable Long managerId,
            @RequestParam String comment) {
        return ResponseEntity.ok(approvalService.rejectExpense(expenseId, managerId, comment));
    }
}
