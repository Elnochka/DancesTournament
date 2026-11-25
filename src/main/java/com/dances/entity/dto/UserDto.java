package com.dances.entity.dto;

import com.dances.entity.Users;
import com.dances.model.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String name;
    private String lastName;
    private RoleEnum role;

    public static UserDto fromEntity(Users user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setName(user.getName());
        dto.setLastName(user.getLastName());

        return dto;
    }
}
