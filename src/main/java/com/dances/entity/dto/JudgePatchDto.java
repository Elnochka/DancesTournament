package com.dances.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgePatchDto {

    private Long id;
    private String nameJudge;
    private Long trainerId;
    private String comment;
}
