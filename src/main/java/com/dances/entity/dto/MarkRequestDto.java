package com.dances.entity.dto;

import com.dances.entity.Mark;
import com.dances.model.RoundEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkRequestDto {

    private Integer startNumber;
    private Long participantTournamentId;
    private Long categoryTournamentId;
    private Long judgeId;
    private Long danceId;
    private RoundEnum round;
    private boolean passed;

    public Mark toEntity() {
        Mark mark = new Mark();
        mark.setRound(this.round);
        mark.setPassed(this.passed);


        return mark;
    }
}
