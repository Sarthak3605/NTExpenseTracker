package com.expenseTrackerProject.expensetracker.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.expenseTrackerProject.expensetracker.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor // this reduce our code at some extinct and it's good
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean //by this spring boot remember this as bean or component
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //encodes the password and return it in Bcrypted format
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS configuration
            .csrf(csrf -> csrf.disable())  // Disable CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers( "/auth/login", "/auth/register","/auth/**").permitAll()  // ✅ Allow login & register

                .requestMatchers("/dashboard/**").hasAnyAuthority("EMPLOYEE", "MANAGER", "FINANCE_TEAM")
                .requestMatchers(HttpMethod.GET,"/dashboard/employee").hasAuthority("EMPLOYEE")
                .requestMatchers("/dashboard/finance").hasAuthority("FINANCE_TEAM")
                .requestMatchers(HttpMethod.GET,"/dashboard/manager").hasRole("MANAGER")

				.requestMatchers(HttpMethod.GET, "/expenses/pending").hasRole("MANAGER")
                .requestMatchers("/expenses/{expenseId}/mark-paid").hasRole("FINANCE_TEAM")
				.requestMatchers("/expenses/**").hasAnyRole("EMPLOYEE","MANAGER","FINANCE_TEAM")
                .requestMatchers("/expenses/finance/**").hasRole("FINANCE_TEAM")  // ✅ Fixed role-based access
				.requestMatchers(HttpMethod.PUT,"/expenses/**").hasAnyRole("MANAGER","EMPLOYEE","FINANCE_TEAM")
				.requestMatchers(HttpMethod.PUT,"/expenses/{expenseId}/update-status").hasAuthority("MANAGER")
				.requestMatchers(HttpMethod.POST,"/expenses/add").hasAnyRole("EMPLOYEE","MANAGER")
				.requestMatchers(HttpMethod.GET,"/expenses/my-expenses").hasRole("EMPLOYEE")
				.requestMatchers("/expenses//delete/{expenseId}").hasRole("EMPLOYEE")
				.requestMatchers(HttpMethod.PUT, "/expenses/**/mark-paid").hasAuthority("FINANCE_TEAM") // ✅ Correct Access

				.requestMatchers("/finance/**").hasRole("FINANCE_TEAM")
				.requestMatchers("/finance/remaining/**").hasRole("FINANCE_TEAM")
				.requestMatchers(HttpMethod.POST,"/budget/**").hasAuthority("FINANCE_TEAM")
				.requestMatchers(HttpMethod.GET,"/finance/pending-payments").hasRole("FINANCE_TEAM")
				.requestMatchers("/departments/add").hasRole("FINANCE_TEAM")

                .anyRequest().authenticated()  // Secure all other endpoints
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Set stateless session
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Add JWT filter

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() { //for frontend purpose
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://127.0.0.1:5500"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
