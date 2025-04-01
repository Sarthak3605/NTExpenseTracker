package com.expenseTrackerProject.expensetracker.repository;

import com.expenseTrackerProject.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
}
