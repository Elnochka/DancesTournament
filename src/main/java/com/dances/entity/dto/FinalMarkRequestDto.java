package com.dances.entity.dto;

import com.dances.entity.FinalMark;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalMarkRequestDto {

    private Long participantId;
    private Long categoryTournamentId;
    private Long judgeId;
    private Long danceId;
    private Integer place;

    public FinalMark toEntity() {
        FinalMark p = new FinalMark();
        p.setPlace(this.place);

        return p;
    }
}
