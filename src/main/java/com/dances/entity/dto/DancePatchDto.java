package com.dances.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DancePatchDto {

    private Long id;
    private String nameDance;
    private String fullNameDance;
    private Long kindOfDanceId;
}
