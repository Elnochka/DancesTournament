package com.dances.service.impl;


import com.dances.entity.Users;
import com.dances.entity.dto.UserDto;
import com.dances.entity.dto.UserRequestUpdateDto;
import com.dances.model.RoleEnum;
import com.dances.repository.UsersRepository;
import com.dances.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;

    @Override
    public List<UserDto> getAllUsers() {

        log.info("Got users successfully. ");

        return usersRepository.findAll()
                .stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getLastName(),
                        user.getRole()
                ))
                .toList();

    }

    @Override
    public Users updateUser(Long id, UserRequestUpdateDto request) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Користувача не знайдено"
                ));

        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        if (request.getRole() != null) {
            user.setRole(RoleEnum.valueOf(request.getRole()));
        }

        log.info("Updated user successfully. ");

        return usersRepository.save(user);
    }
}
