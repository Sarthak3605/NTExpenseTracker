package com.expenseTrackerProject.expensetracker.dto; //data transfer object transfer the data between layers easily

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
}
