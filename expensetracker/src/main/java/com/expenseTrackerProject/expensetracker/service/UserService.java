package com.expenseTrackerProject.expensetracker.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.expenseTrackerProject.expensetracker.model.Department;
import com.expenseTrackerProject.expensetracker.model.Role;
import com.expenseTrackerProject.expensetracker.model.User;
import com.expenseTrackerProject.expensetracker.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service //mark as service
@RequiredArgsConstructor //no need for argument constructors
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

	public User getAuthenticatedUser() { //authenticate the users
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    String email = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();

    return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
}

//register the user with certain parameters

    public User registerUser(String name, String email, String password, Role role, Department department) {
        if (!email.endsWith("@gmail.com")) {                  /* I have to modify this to @NucleusTeq.com */
            throw new RuntimeException("Only company email addresses are allowed.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists with this email!");
        }

        //Ensure role and department are valid (Enum validation)
        if (role == null || department == null) {
            throw new IllegalArgumentException("Role and Department must be provided.");
        }
        //create new user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setDepartment(department);

        return userRepository.save(user);
    }
}
