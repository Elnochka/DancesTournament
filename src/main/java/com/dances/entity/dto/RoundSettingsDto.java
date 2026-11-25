package com.dances.entity.dto;

import com.dances.entity.RoundSettings;
import com.dances.model.RoundEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundSettingsDto {

    private Long id;
    private Long categoryTournamentId;
    private String categoryTournamentName;
    private RoundEnum round;
    private Integer roundOrder;
    private Integer couplesToNextRound;
//    private Integer participantsCount;
//    private Integer recommendedCouplesToNextRound;

    public static RoundSettingsDto fromEntity(RoundSettings p) {

        RoundSettingsDto dto = new RoundSettingsDto();
        dto.setId(p.getId());
        dto.setCouplesToNextRound(p.getCouplesToNextRound());
        dto.setRoundOrder(p.getRoundOrder());
        dto.setRound(p.getRound());
        dto.setCategoryTournamentId(p.getCategoryTournament() != null ? p.getCategoryTournament().getId(): null);
        dto.setCategoryTournamentName(p.getCategoryTournament() != null ?
                p.getCategoryTournament().getNameCategory(): null);

        return dto;

    }
}
