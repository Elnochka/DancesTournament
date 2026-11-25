package com.dances.entity.dto;

import com.dances.entity.Couple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoupleRequestDto {

    private String nameCouple;
    private Long participantFemaleId;
    private Long participantMaleId;

    public Couple toEntity() {

        Couple p = new Couple();
        p.setNameCouple(this.nameCouple);
        return p;

    }
}
