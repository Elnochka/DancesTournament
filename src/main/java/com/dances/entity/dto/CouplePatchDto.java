package com.dances.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouplePatchDto {

    private Date dateTournamentP;
    private String nameCouple;
    private Long participantFemaleId;
    private Long participantMaleId;

}
