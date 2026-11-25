package com.dances.entity.dto;

import com.dances.entity.RoundSettings;
import com.dances.model.RoundEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoundSettingsRequestDto {

    private Long id;
    private Long categoryTournamentId;
    private RoundEnum round;
    private Integer roundOrder;
    private Integer couplesToNextRound;
//    private Integer participantsCount;
//    private Integer recommendedCouplesToNextRound;

    public RoundSettings toEntity() {

        RoundSettings p = new RoundSettings();
        p.setCouplesToNextRound(this.couplesToNextRound);
        p.setRoundOrder(this.roundOrder);
        p.setRound(this.round);

        return p;
    }


}
