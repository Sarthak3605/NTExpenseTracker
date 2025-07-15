package com.expenseTrackerProject.expensetracker.security;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "X7gH9zL1pQwR5dT3mVbN8aK2YcFJ6MZSX7gH9zP1pQwR5dT3mVbN8aK2YcFJ6MZS"; //our key in property.application i store

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); //Converts the base64 (represent binary data) secret key to a real cryptographic key
        return Keys.hmacShaKeyFor(keyBytes); //this require by jwt to sign and verify the user
    }

    public String generateToken(String email,String role,String department) { //this generate the token and add the details
		Map<String,Object> claims = new HashMap<>(); //claims are small pieces used to store info
		claims.put("role",role);
        return Jwts.builder()
		        .setClaims(claims)
                .setSubject(email)
				.claim("department",department)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1-hour expiration
                .signWith(getSigningKey()) // Automatically uses HS256
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject); //extract the email
    }

	public String extractDepartment(String token) {
		Claims claims = extractAllClaims(token);
		return claims.get("department", String.class); //extracts as a string type
	}

	public List<String> extractRoles(String token) {
    Claims claims = extractAllClaims(token);
    Object roleClaim = claims.get("role"); //extract and store the role of user

    if (roleClaim instanceof String) {
        return Collections.singletonList(roleClaim.toString());
    }

    return Collections.emptyList();
}

private Claims extractAllClaims(String token) { //Fixed missing method
	return Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody();
}

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean validateToken(String token, String email) {
        return (extractEmail(token).equals(email)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
