package com.dances.entity.dto;

import com.dances.entity.Judge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeRequestDto {

    private String nameJudge;
    private Long trainerId;
    private String comment;


    public Judge toEntity() {
        Judge judge = new Judge();
        judge.setNameJudge(this.nameJudge);
        judge.setComment(this.comment);

        return judge;
    }
}
