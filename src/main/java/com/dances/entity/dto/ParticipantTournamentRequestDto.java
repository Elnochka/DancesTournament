package com.dances.entity.dto;

import com.dances.entity.ParticipantTournament;
import com.dances.model.SoloCoupleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantTournamentRequestDto {

    private Integer startNumber;
    private Long participantId;
    private Long categoryTournamentId;
    private Long coupleId;
    private SoloCoupleEnum soloCouple;


    public ParticipantTournament toEntity() {

        ParticipantTournament entity = new ParticipantTournament();
        entity.setStartNumber(this.startNumber);
        entity.setSoloCouple(this.soloCouple);

        return entity;
    }
}
