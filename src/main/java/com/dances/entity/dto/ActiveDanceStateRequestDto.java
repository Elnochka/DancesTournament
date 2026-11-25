package com.dances.entity.dto;

import com.dances.entity.ActiveDanceState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiveDanceStateRequestDto {

    private Long categoryTournamentId;
    private Long roundSettingsId;
    private Long danceId;

    public ActiveDanceState toEntity() {

        ActiveDanceState entity = new ActiveDanceState();
        entity.setDanceId(this.danceId);
        entity.setCategoryTournamentId(this.categoryTournamentId);
        entity.setRoundSettingsId(this.roundSettingsId);

        return entity;

    }
}
