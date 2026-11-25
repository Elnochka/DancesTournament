package com.dances.entity.dto;

import com.dances.entity.TrainerClubHistory;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainerClubHistoryDto {

    private Long id;
    private Long clubId;
    private Long trainerId;
    private LocalDate startDate;
    private LocalDate endDate;

    public static TrainerClubHistoryDto fromEntity(TrainerClubHistory e) {

        TrainerClubHistoryDto d = new TrainerClubHistoryDto();
        d.setId(e.getId());
        d.setClubId(e.getClub() != null ? e.getClub().getId() : null);
        d.setTrainerId(e.getTrainer() != null ? e.getTrainer().getId() : null);
        d.setStartDate(e.getStartDate());
        d.setEndDate(e.getEndDate());

        return d;

    }
}
