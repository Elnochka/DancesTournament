package com.dances.service;

import com.dances.entity.Users;
import com.dances.entity.dto.LoginDto;
import com.dances.entity.dto.RegisterDto;
import com.dances.model.RoleEnum;
import com.dances.repository.UsersRepository;
import com.dances.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Map<String, String> register(RegisterDto dto) {

        if (usersRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("User with email already exists: " + dto.getEmail());
        }

        Users user = new Users();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole() != null ? dto.getRole() : RoleEnum.DANCER);
        usersRepository.save(user);

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return Map.of("access", access, "refresh", refresh);
    }

    public Map<String, String> login(LoginDto dto) {
        Users user = usersRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Bad credentials");
        }

        return Map.of(
                "access", jwtService.generateAccessToken(user),
                "refresh", jwtService.generateRefreshToken(user)
        );
    }

    public Map<String, String> refresh(String refreshToken) {

        String username = jwtService.extractUsername(refreshToken);
        Users user = usersRepository.findByEmail(username).orElseThrow();

        if (!jwtService.isValid(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccess = jwtService.generateAccessToken(user);

        return Map.of("access", newAccess);
    }

    public Optional<Users> getByEmail(String email) {
        System.out.println(email);
        return usersRepository.findByEmail(email);
    }

    public Optional<Users> getMe(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }

        String token = authorizationHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return Optional.ofNullable(usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

//    public Users registerUser(RegisterDto dto) {
//        if (usersRepository.existsByEmail(dto.getEmail())) {
//            throw new RuntimeException("User with email already exists: " + dto.getEmail());
//        }
//
//        Users user = new Users();
//        user.setEmail(dto.getEmail());
//        user.setPassword(passwordEncoder.encode(dto.getPassword()));
//        user.setName(dto.getName());
//        user.setLastName(dto.getLastName());
//        user.setActive(true);
//        user.setRole(dto.getRole() != null ? dto.getRole() : RoleEnum.DANCER);
//
//        usersRepository.save(user);
//        return user;
//    }
//
//    public Optional<Users> findByUsername(String username) {
//        System.out.println(username);
//        return usersRepository.findByName(username);
//    }
}
