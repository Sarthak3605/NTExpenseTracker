package com.expenseTrackerProject.expensetracker.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"budget", "users", "expenses"})
public class Department {
    @Id //mark as primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto increment built-in
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

	@JsonIgnore  // Prevents circular reference
    @OneToMany(mappedBy = "department",cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@JsonManagedReference
    private List<User> users;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true,fetch= FetchType.LAZY)
	@JsonIgnore
private List<Expense> expenses;

    @OneToOne(mappedBy = "department")
    private Budget budget;
}
