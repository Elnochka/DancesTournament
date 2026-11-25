package com.dances.entity.dto;

import com.dances.model.RoundEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkPatchDto {

    private Long participantTournamentId;
    private Long categoryTournamentId;
    private Long judgeId;
    private Long danceId;
    private RoundEnum round;
    private boolean passed;
}
