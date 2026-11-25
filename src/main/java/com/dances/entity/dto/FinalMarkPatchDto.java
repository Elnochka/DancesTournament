package com.dances.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinalMarkPatchDto {

    private Long participantId;
    private Long categoryTournamentId;
    private Long judgeId;
    private Long danceId;
    private Integer place;
}
