package com.dances.entity.dto;

import com.dances.model.SoloCoupleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantTournamentPatchDto {

    private Integer startNumber;
    private Long participantId;
    private Long categoryTournamentId;
    private Long coupleId;
    private SoloCoupleEnum soloCouple;
}
