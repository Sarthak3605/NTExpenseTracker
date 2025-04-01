package com.expenseTrackerProject.expensetracker.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*; //this will reduce the need of getters, setters. toString...etc

@Entity //declare the entity
@Table(name = "users") //connected to the table users
@Data //lombok
@NoArgsConstructor
@AllArgsConstructor
@Builder //easy tio use object creation using builder built
@ToString(exclude = "department")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) //ensure the column is not null
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne //many to one as many users belong to one department
    @JoinColumn(name = "department_id", nullable = false)
	@JsonIgnore
    private Department department;
}
