package com.dances.security;

import com.dances.entity.Users;
import com.dances.model.RoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "my-test-secret-key-for-jwt-service-123456789";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        Users user = new Users();
        user.setEmail("admin@example.com");
        user.setRole(RoleEnum.ADMIN);

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                "admin@example.com",
                jwtService.extractUsername(token)
        );
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        Users user = new Users();
        user.setEmail("admin@example.com");

        String token = jwtService.generateRefreshToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                "admin@example.com",
                jwtService.extractUsername(token)
        );
    }

    @Test
    void isValid_withCorrectUser_shouldReturnTrue() {
        Users user = new Users();
        user.setEmail("admin@example.com");

        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.isValid(token, user));
    }

    @Test
    void isValid_withDifferentUser_shouldReturnFalse() {
        Users tokenOwner = new Users();
        tokenOwner.setEmail("admin@example.com");

        Users anotherUser = new Users();
        anotherUser.setEmail("judge@example.com");

        String token = jwtService.generateAccessToken(tokenOwner);

        assertFalse(jwtService.isValid(token, anotherUser));
    }

    @Test
    void isValid_withExpiredToken_shouldReturnFalse() {
        Users user = new Users();
        user.setEmail("admin@example.com");

        String expiredToken = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(
                        Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();

        assertFalse(jwtService.isValid(expiredToken, user));
    }

    @Test
    void isValid_withMalformedToken_shouldReturnFalse() {
        Users user = new Users();
        user.setEmail("admin@example.com");

        assertFalse(jwtService.isValid("invalid-token", user));
    }

    @Test
    void generateAccessToken_shouldContainUserRole() {
        Users user = new Users();
        user.setEmail("admin@example.com");
        user.setRole(RoleEnum.ADMIN);

        String token = jwtService.generateAccessToken(user);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))
                )
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("admin@example.com", claims.getSubject());
        assertNotNull(claims.get("role"));
        assertEquals(RoleEnum.ADMIN.toString(), claims.get("role"));
    }

    @Test
    void isValid_withWrongSignature_shouldReturnFalse() {
        Users user = new Users();
        user.setEmail("admin@example.com");

        String wrongSecret = "another-test-secret-key-for-jwt-123456";

        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(
                        Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS256
                )
                .compact();

        assertFalse(jwtService.isValid(token, user));
    }

    @Test
    void extractUsername_shouldReturnEmailFromToken() {
        Users user = new Users();
        user.setEmail("admin@example.com");
        user.setRole(RoleEnum.ADMIN);

        String token = jwtService.generateAccessToken(user);

        String username = jwtService.extractUsername(token);

        assertEquals(user.getEmail(), username);
    }
}
