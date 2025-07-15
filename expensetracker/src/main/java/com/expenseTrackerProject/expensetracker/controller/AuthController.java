package com.expenseTrackerProject.expensetracker.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expenseTrackerProject.expensetracker.dto.LoginRequest;
import com.expenseTrackerProject.expensetracker.dto.UserRegistrationRequest;
import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.model.Role;
import com.expenseTrackerProject.expensetracker.model.User;
import com.expenseTrackerProject.expensetracker.repository.DepartmentRepository;
import com.expenseTrackerProject.expensetracker.repository.UserRepository;
import com.expenseTrackerProject.expensetracker.security.JwtUtil;
import com.expenseTrackerProject.expensetracker.service.AuthService;

import lombok.RequiredArgsConstructor;


@CrossOrigin(origins = "*") //removes crossorigin errors for frontend part and allows for all origins
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor //reduce the code make it very readable and good
public class AuthController{

    private final UserRepository userRepository;
    private final AuthService authService;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		// Find user by email
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));

				System.out.println("Stored Password: " + user.getPassword()); // double check debug purpose
				System.out.println("Entered Password: " + request.getPassword());

		// Check password match
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}

		// Generate JWT token
		String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(),user.getDepartment().getName());

		return ResponseEntity.ok(token);
	}



    //Fixed Registration Method
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {

		if (!request.getEmail().endsWith("@gmail.com")) {
			return ResponseEntity.badRequest().body("Invalid Company email. Use @gmail.com");
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			return ResponseEntity.badRequest().body("Error: Email already in use!"); //Prevent duplicate email
		}

		String departmentName = request.getDepartment().trim();
		Optional<Department> departmentOpt = departmentRepository.findByNameIgnoreCase(departmentName);

		if (departmentOpt.isEmpty()) {
			return ResponseEntity.badRequest().body("Invalid Department: " + departmentName);
		}

		Department department = departmentOpt.get();

		try {
			User user = authService.registerUser(
					request.getName(),
					request.getEmail(),
					request.getPassword(), //Send raw password
					Role.valueOf(request.getRole().toUpperCase()),
					department
			);
			return ResponseEntity.ok("User registered successfully!");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body("Invalid Role: " + request.getRole());
		}
	}

}
