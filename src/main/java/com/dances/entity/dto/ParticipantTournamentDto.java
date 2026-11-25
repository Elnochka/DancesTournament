package com.dances.entity.dto;

import com.dances.entity.ParticipantTournament;
import com.dances.model.SoloCoupleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantTournamentDto {

    private Long id;
    private Integer startNumber;
    private Long participantId;
    private Long categoryTournamentId;
    private Long coupleId;
    private String participantName;
    private String categoryTournamentName;
    private String coupleName;
    private SoloCoupleEnum soloCouple;
    private String displayName;

    public static ParticipantTournamentDto fromEntity(ParticipantTournament entity) {
        ParticipantTournamentDto dto = new ParticipantTournamentDto();
        dto.setId(entity.getId());
        dto.setStartNumber(entity.getStartNumber());
        if (entity.getParticipant() != null) {
            dto.setParticipantName(entity.getParticipant().getFirstnameUkr() + " " + entity.getParticipant().getLastnameUkr());
            dto.setParticipantId(entity.getParticipant().getId());
        }
        if (entity.getCategoryTournament() != null) {
            dto.setCategoryTournamentName(entity.getCategoryTournament().getNameCategory());
            dto.setCategoryTournamentId(entity.getCategoryTournament().getId());
        }
        if (entity.getCouple() != null) {
            dto.setCoupleName(entity.getCouple().getNameCouple());
            dto.setCoupleId(entity.getCouple().getId());
        }
        dto.setSoloCouple(entity.getSoloCouple());
        dto.setDisplayName(entity.getDisplayName());

        return dto;
    }
}
