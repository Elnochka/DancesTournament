package com.dances.service;

import com.dances.entity.Users;
import com.dances.entity.dto.UserDto;
import com.dances.entity.dto.UserRequestUpdateDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    Users updateUser(Long id, UserRequestUpdateDto request);
}
