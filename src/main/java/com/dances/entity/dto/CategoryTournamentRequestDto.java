package com.dances.entity.dto;

import com.dances.entity.CategoryTournament;
import com.dances.model.SoloCoupleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTournamentRequestDto {

    private Long id;
    private String nameCategory;
    private SoloCoupleEnum soloCouple;
    private Long klassId;
    private Long categoryParticipantId;
    private Long kindOfDanceId;

    public CategoryTournament toEntity() {

        CategoryTournament entity = new CategoryTournament();
        entity.setId(this.id);
        entity.setNameCategory(this.nameCategory);
        entity.setSoloCouple(this.soloCouple);
        return entity;

    }

}
