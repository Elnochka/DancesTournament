package com.dances.entity.dto;

import com.dances.entity.ParticipantTrainerHistory;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ParticipantTrainerHistoryDto {

    private Long id;
    private Long participantId;
    private Long trainerId;
    private LocalDate startDate;
    private LocalDate endDate;

    public static ParticipantTrainerHistoryDto fromEntity(ParticipantTrainerHistory e) {
        ParticipantTrainerHistoryDto d = new ParticipantTrainerHistoryDto();
        d.setId(e.getId());
        d.setParticipantId(e.getParticipant() != null ? e.getParticipant().getId() : null);
        d.setTrainerId(e.getTrainer() != null ? e.getTrainer().getId() : null);
        d.setStartDate(e.getStartDate());
        d.setEndDate(e.getEndDate());
        return d;
    }
}
