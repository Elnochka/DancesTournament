package com.dances.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerPatchDto {

    private String nameTrainer;
    private Long clubId;
    private Long categoryJudgeId;
    private Long organizationId;
    private Long cityId;
    private Long countryId;
}
