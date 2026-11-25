package com.dances.entity.dto;

import com.dances.entity.Mark;
import com.dances.model.RoundEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkDto {

    private Long id;
    private Integer startNumber;
    private Long categoryTournamentId;
    private Long judgeId;
    private Long participantId;
    private String categoryTournament;
    private String judge;
    private Long danceId;
    private String dance;
    private RoundEnum round;
    private boolean passed;
    private boolean locked;


    public static MarkDto fromEntity(Mark mark) {

        MarkDto dto = new MarkDto();
        dto.setId(mark.getId());
        dto.setStartNumber(mark.getParticipantTournament() != null ?
                mark.getParticipantTournament().getStartNumber() : null);
        dto.setCategoryTournamentId(mark.getCategoryTournament() != null ?
                mark.getCategoryTournament().getId() : null);
        dto.setCategoryTournament(mark.getCategoryTournament() != null ?
                mark.getCategoryTournament().getNameCategory() : null);
        dto.setJudgeId(mark.getJudge() != null ?
                mark.getJudge().getId() : null);
        dto.setParticipantId(mark.getParticipantTournament() != null ?
                mark.getParticipantTournament().getId() : null);
        dto.setJudge(mark.getJudge() != null ?
                mark.getJudge().getNameJudge() : null);
        dto.setDanceId(mark.getDance() != null ?
                mark.getDance().getId() : null);
        dto.setDance(mark.getDance() != null ?
                mark.getDance().getNameDance() : null);
        dto.setRound(mark.getRound());
        dto.setPassed(mark.isPassed());
        dto.setLocked(mark.isLocked());
        return dto;

    }
}
