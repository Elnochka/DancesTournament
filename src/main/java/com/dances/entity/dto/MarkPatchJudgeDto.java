package com.dances.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkPatchJudgeDto {

    private Integer startNumber;
    private boolean passed;
}
