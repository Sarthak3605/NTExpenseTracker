package com.expenseTrackerProject.expensetracker.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.expenseTrackerProject.expensetracker.model.Budget;
import com.expenseTrackerProject.expensetracker.model.Department;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Ensure we get only the latest budget entry for a department
    @Query("SELECT b FROM Budget b WHERE b.department = :department ORDER BY b.endDate DESC")
    Optional<Budget> findLatestBudgetByDepartment(@Param("department") Department department);

	Optional<Budget> findByDepartment(Department department); //optional is use for conditions, and manage them easily

	    // Corrected method declaration
		Optional<Budget> findByDepartment_Id(Long departmentId);
}

