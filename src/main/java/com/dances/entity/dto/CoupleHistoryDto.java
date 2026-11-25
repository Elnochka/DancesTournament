package com.dances.entity.dto;

import com.dances.entity.CoupleHistory;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CoupleHistoryDto {

    private Long id;
    private Long coupleId;
    private Long maleId;
    private Long femaleId;
    private LocalDate startDate;
    private LocalDate endDate;

    public static CoupleHistoryDto fromEntity(CoupleHistory e) {
        CoupleHistoryDto d = new CoupleHistoryDto();
        d.setId(e.getId());
        d.setCoupleId(e.getCouple() != null ? e.getCouple().getId() : null);
        d.setMaleId(e.getMaleParticipant() != null ? e.getMaleParticipant().getId() : null);
        d.setFemaleId(e.getFemaleParticipant() != null ? e.getFemaleParticipant().getId() : null);
        d.setStartDate(e.getStartDate());
        d.setEndDate(e.getEndDate());
        return d;
    }
}
