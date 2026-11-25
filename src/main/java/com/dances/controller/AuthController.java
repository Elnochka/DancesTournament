package com.dances.controller;

import com.dances.entity.dto.LoginDto;
import com.dances.entity.dto.RegisterDto;
import com.dances.entity.dto.UserDto;
import com.dances.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для реєстрації та логіну користувачів")
public class AuthController {

    private final UsersService usersService;

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterDto dto) {
        return usersService.register(dto);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginDto dto) {
        return usersService.login(dto);
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh(@RequestBody Map<String, String> body) {
        return usersService.refresh(body.get("refresh"));
    }

    @Operation(summary = "Отримати user за email (DTO)")
    @GetMapping("/{email}")
    public ResponseEntity<UserDto> getByEmail(@PathVariable String email) {
        return usersService.getByEmail(email)
                .map(users -> ResponseEntity.ok(UserDto.fromEntity(users)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestHeader("Authorization") String authHeader) {
        return usersService.getMe(authHeader)
                .map(users -> ResponseEntity.ok(UserDto.fromEntity(users)))
                .orElse(ResponseEntity.notFound().build());
    }

}
