package com.dances.entity.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestUpdateDto {

    private String name;
    private String lastName;
    private String email;
    private String role;

}
