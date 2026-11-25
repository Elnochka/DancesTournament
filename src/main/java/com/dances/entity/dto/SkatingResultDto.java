package com.dances.entity.dto;

import com.dances.entity.SkatingResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkatingResultDto {

    private Long id;
    private Integer startNumber;
    private Long participantTournamentId;
    private Long categoryTournamentId;
    private String participantTournament;
    private String categoryTournament;
    private BigDecimal finalPlace;

    public static SkatingResultDto fromEntity(SkatingResult entity) {

        SkatingResultDto dto = new SkatingResultDto();
        dto.setId(entity.getId());
        dto.setStartNumber(entity.getParticipantTournament() != null ?
                entity.getParticipantTournament().getStartNumber() : null);
        dto.setParticipantTournamentId(entity.getParticipantTournament() != null ? entity.getParticipantTournament().getId() : null);
        dto.setParticipantTournament(entity.getParticipantTournament() != null ? entity.getParticipantTournament().getDisplayName() : null);
        dto.setCategoryTournamentId(entity.getCategoryTournament() != null ? entity.getCategoryTournament().getId() : null);
        dto.setCategoryTournament(entity.getCategoryTournament() != null ?
                entity.getCategoryTournament().getNameCategory() : null);
        dto.setFinalPlace(entity.getFinalPlace());

        return dto;
    }
}
