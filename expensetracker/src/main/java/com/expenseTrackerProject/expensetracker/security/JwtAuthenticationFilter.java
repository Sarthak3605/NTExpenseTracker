package com.expenseTrackerProject.expensetracker.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter { //OncePerRequestFilter ensures filter run once as per one request
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService; //this just look for details of user

	public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException { //this will run for all http request

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        } //this tells that header must contain Bearer "Token"

        String token = authHeader.substring(7); //substring because this removes the Bearer and only pure token is stored in the variable
        String email = jwtUtil.extractEmail(token);
		List<String> roles = jwtUtil.extractRoles(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) { //run or execute when user is not already authenticated
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.validateToken(token, userDetails.getUsername())) {  //Passing userDetails instead of email
				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(request, response); //continue the chain
    }
}
