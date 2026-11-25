package com.dances.entity.dto;

import com.dances.entity.Trainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainerRequestDto {

    private String nameTrainer;
    private Long clubId;
    private Long categoryJudgeId;
    private Long organizationId;
    private Long cityId;
    private Long countryId;

    public Trainer toEntity() {

        Trainer trainer = new Trainer();
        trainer.setNameTrainer(this.nameTrainer);

        return trainer;

    }
}
