package com.expenseTrackerProject.expensetracker.repository;

import com.expenseTrackerProject.expensetracker.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByNameIgnoreCase(String name);  // Fetch department by name
	Department findByName(String name);
	boolean existsByName(String name);
}
