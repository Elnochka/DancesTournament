package com.dances.controller;


import com.dances.entity.Users;
import com.dances.entity.dto.UserDto;
import com.dances.entity.dto.UserRequestUpdateDto;
import com.dances.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers(Authentication authentication) {
        checkAdmin(authentication);

        log.info("Getting all users");

        return userService.getAllUsers();
    }

    @PatchMapping("/{id}")
    public Users updateUser(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UserRequestUpdateDto request) {

        checkAdmin(authentication);

        log.info("Updating user. userId={}", id);

        return userService.updateUser(id, request);
    }

    private void checkAdmin(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();

        if (!"ADMIN".equals(user.getRole().toValue())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied"
            );
        }
    }
}
