package com.dances.entity.dto;

import com.dances.entity.Dance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DanceRequestDto {

    private String nameDance;
    private String fullNameDance;
    private Long kindOfDanceId;

    public Dance toEntity() {
        Dance p = new Dance();
        p.setNameDance(this.nameDance);
        p.setFullNameDance(this.fullNameDance);

        return p;
    }
}
