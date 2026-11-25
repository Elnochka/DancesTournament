package com.dances.entity.dto;

import com.dances.entity.Participant;
import com.dances.model.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRequestDto {

    private String firstnameUkr;
    private String lastnameUkr;
    private Long countryId;
    private LocalDate dateBirth;
    private Long trainerId;
    private GenderEnum gender;
    private String firstnameEn;
    private String lastnameEn;
    private Long klassId;
    private List<Long> kindOfDanceIds;

    public Participant toEntity() {

        Participant p = new Participant();
        p.setFirstnameUkr(this.firstnameUkr);
        p.setLastnameUkr(this.lastnameUkr);
        p.setDateBirth(this.dateBirth);
        p.setGender(this.gender);
        p.setFirstnameEn(this.firstnameEn);
        p.setLastnameEn(this.lastnameEn);
        return p;

    }
}
