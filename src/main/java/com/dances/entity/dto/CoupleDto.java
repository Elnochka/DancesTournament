package com.dances.entity.dto;

import com.dances.entity.Couple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoupleDto {

    private Long id;
    private String nameCouple;

    private Long participantFemaleId;
    private Long participantMaleId;
    private String participantFemaleName;
    private String participantMaleName;


    public static CoupleDto fromEntity(Couple p) {
        CoupleDto dto = new CoupleDto();
        dto.setId(p.getId());
        dto.setNameCouple(p.getNameCouple());
        dto.setParticipantMaleId(p.getParticipantMale() != null ? p.getParticipantMale().getId(): null);
        dto.setParticipantFemaleId(p.getParticipantFemale() != null ? p.getParticipantFemale().getId(): null);
        dto.setParticipantMaleName(p.getParticipantMale() != null ?
                p.getParticipantMale().getFirstnameUkr() + " " + p.getParticipantMale().getLastnameUkr(): null);
        dto.setParticipantFemaleName(p.getParticipantFemale() != null ?
                p.getParticipantFemale().getFirstnameUkr() + " "  + p.getParticipantFemale().getLastnameUkr() : null);

        return dto;
    }
}
