package com.dances.entity.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequest {

    private String email;
    private String password;
    private String name;
    private String lastName;
}
