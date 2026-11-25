package com.dances.entity.dto;

import com.dances.entity.Dance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DanceDto {

    private Long id;
    private String nameDance;
    private String fullNameDance;

    private Long kindOfDanceId;
    private String kindOfDanceName;

    public static DanceDto fromEntity(Dance p) {

        DanceDto dto = new DanceDto();
        dto.setId(p.getId());
        dto.setNameDance(p.getNameDance());
        dto.setKindOfDanceName(p.getKindOfDance() != null ?
                p.getKindOfDance().getNameKindOfDance() : null);
        dto.setKindOfDanceId(p.getKindOfDance() != null ?
                p.getKindOfDance().getId() : null);
        dto.setFullNameDance(p.getFullNameDance());
        return dto;
    }
}
