package com.dances.entity.dto;

import com.dances.entity.SkatingResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkatingResultRequestDto {

    private Long participantId;
    private Long categoryTournamentId;
    private BigDecimal finalPlace;

    public SkatingResult toEntity() {
        SkatingResult p = new SkatingResult();
        p.setFinalPlace(this.finalPlace);

        return p;
    }
}
