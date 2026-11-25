package com.dances.entity.dto;

import com.dances.entity.Judge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeDto {

    private Long id;
    private String nameJudge;
    private Long trainerId;
    private String trainerName;
    private String comment;

    public static JudgeDto fromEntity(Judge p) {

        JudgeDto dto = new JudgeDto();
        dto.setId(p.getId());
        dto.setNameJudge(p.getNameJudge());
        dto.setTrainerId(p.getTrainer() != null ? p.getTrainer().getId() : null);
        dto.setTrainerName(p.getTrainer() != null ?
                p.getTrainer().getNameTrainer() : null);
        dto.setComment(p.getComment());
        return dto;
    }
}
