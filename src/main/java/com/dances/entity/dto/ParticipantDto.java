package com.dances.entity.dto;

import com.dances.entity.Participant;
import com.dances.model.GenderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDto {

    private Long id;
    private String firstnameUkr;
    private String lastnameUkr;
    private String firstnameEn;
    private String lastnameEn;
    private Long countryId;
    private String countryName;
    private GenderEnum gender;
    private LocalDate dateBirth;
    private Long trainerId;
    private String trainerName;
    private Long klassId;
    private String klassName;
    private Set<KindOfDanceDto> kindOfDances;

    public static ParticipantDto fromEntity(Participant p) {
        ParticipantDto dto = new ParticipantDto();
        dto.setId(p.getId());
        dto.setFirstnameUkr(p.getFirstnameUkr());
        dto.setLastnameUkr(p.getLastnameUkr());
        dto.setFirstnameEn(p.getFirstnameEn());
        dto.setLastnameEn(p.getLastnameEn());

        dto.setKindOfDances(p.getKindOfDances().stream()
                .map(k -> KindOfDanceDto.builder()
                        .id(k.getId())
                        .name(k.getNameKindOfDance())
                        .build())
                .collect(Collectors.toSet()));

        dto.setGender(p.getGender());
        dto.setDateBirth(p.getDateBirth());
        dto.setTrainerId(p.getTrainer() != null ? p.getTrainer().getId(): null);
        dto.setTrainerName(p.getTrainer() != null ? p.getTrainer().getNameTrainer() : null);
        dto.setKlassId(p.getKlass() != null ? p.getKlass().getId() : null);
        dto.setKlassName(p.getKlass() != null ? p.getKlass().getNameKlass() : null);
        dto.setCountryId(p.getCountry() != null ? p.getCountry().getId() : null);
        dto.setCountryName(p.getCountry() != null ? p.getCountry().getNameCountry() : null);
        return dto;
    }
}
