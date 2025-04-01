package com.expenseTrackerProject.expensetracker.controller;

import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PreAuthorize("hasRole('FINANCE_TEAM')") //only access by finance team members
    @PostMapping("/add") //to add the department
    public ResponseEntity<String> addDepartment(@RequestBody Department department) {
        try {
            departmentService.addDepartment(department);
            return ResponseEntity.ok("Department added successfully.");
        } catch(Exception e){
			System.err.println("Error while adding department: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
