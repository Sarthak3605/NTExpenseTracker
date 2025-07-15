package com.expenseTrackerProject.expensetracker.service;

import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    // Add a new department
    public Department addDepartment(Department department) {
		if (departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department already exists.");
        }
        return departmentRepository.save(department);
    }

    // Get all departments
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }
}
