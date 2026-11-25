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
public class ActiveDanceStateDto {

    private Long categoryTournamentId;
    private Long roundSettingsId;
    private Long danceId;

    public static ActiveDanceStateDto fromEntity(ActiveDanceState entity) {
        if (entity == null) return null;

        return ActiveDanceStateDto.builder()
                .categoryTournamentId(entity.getCategoryTournamentId())
                .roundSettingsId(entity.getRoundSettingsId())
                .danceId(entity.getDanceId())
                .build();

    }
}
