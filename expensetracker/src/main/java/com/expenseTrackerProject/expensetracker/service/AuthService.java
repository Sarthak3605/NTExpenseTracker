package com.expenseTrackerProject.expensetracker.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.model.Role;
import com.expenseTrackerProject.expensetracker.model.User;
import com.expenseTrackerProject.expensetracker.repository.UserRepository;
import com.expenseTrackerProject.expensetracker.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


	public String login(String email, String password) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new BadCredentialsException("Invalid credentials")); //Check if user exists

		//Compare hashed password
		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new BadCredentialsException("Invalid credentials"); //If password doesn't match
		}

		//Authenticate user in Spring Security built-in
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
		return jwtUtil.generateToken(user.getEmail(),user.getRole().name(),user.getDepartment().getName());
	}

	public User registerUser(String name, String email, String password, Role role, Department department) {

        // Create a new user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setDepartment(department);

        return userRepository.save(user);
    }
}

