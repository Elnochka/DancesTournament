package com.dances.entity.dto;

import com.dances.entity.CategoryTournament;
import com.dances.model.SoloCoupleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTournamentDto {

    private Long id;
    private String nameCategory;
    private SoloCoupleEnum soloCouple;
    private Long klassId;
    private Long categoryParticipantId;
    private Long kindOfDanceId;
    private String klassName;
    private String categoryParticipantName;
    private String kindOfDanceName;

    public static CategoryTournamentDto fromEntity(CategoryTournament entity) {
        if (entity == null) return null;

        return CategoryTournamentDto.builder()
                .id(entity.getId())
                .nameCategory(entity.getNameCategory())
                .soloCouple(entity.getSoloCouple())
                .klassId(entity.getKlass() != null ? entity.getKlass().getId() : null)
                .klassName(entity.getKlass() != null ? entity.getKlass().getNameKlass() : null)
                .categoryParticipantId(entity.getCategoryParticipant() != null ?
                        entity.getCategoryParticipant().getId() : null)
                .categoryParticipantName(entity.getCategoryParticipant() != null ?
                        entity.getCategoryParticipant().getNameCategory() : null)
                .kindOfDanceName(entity.getKindOfDance() != null ?
                        entity.getKindOfDance().getNameKindOfDance() : null)
                .kindOfDanceId(entity.getKindOfDance() != null ?
                        entity.getKindOfDance().getId() : null)
                .build();
    }

}
