package com.dances.entity.dto;

import com.dances.entity.FinalMark;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalMarkDto {

    private Long id;
    private Integer startNumber;
    private Long participantId;
    private Long categoryTournamentId;
    private Long judgeId;
    private String categoryTournament;
    private String judge;
    private Long danceId;
    private String dance;
    private Integer place;
    private boolean locked;

    public static FinalMarkDto fromEntity(FinalMark entity) {

        FinalMarkDto dto = new FinalMarkDto();
        dto.setId(entity.getId());
        dto.setStartNumber(entity.getParticipantTournament() != null ?
                entity.getParticipantTournament().getStartNumber() : null);
        dto.setParticipantId(entity.getParticipantTournament() != null ?
                entity.getParticipantTournament().getId() : null);
        dto.setCategoryTournamentId(entity.getCategoryTournament() != null ?
                entity.getCategoryTournament().getId() : null);
        dto.setCategoryTournament(entity.getCategoryTournament() != null ?
                entity.getCategoryTournament().getNameCategory() : null);
        dto.setJudge(entity.getJudge() != null ?
                entity.getJudge().getNameJudge() : null);
        dto.setJudgeId(entity.getJudge() != null ?
                entity.getJudge().getId() : null);
        dto.setDanceId(entity.getDance() != null ?
                entity.getDance().getId() : null);
        dto.setDance(entity.getDance() != null ?
                entity.getDance().getNameDance() : null);
        dto.setPlace(entity.getPlace());
        dto.setLocked(entity.isLocked());

        return dto;
    }

}
