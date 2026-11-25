package com.dances.entity.dto;

import com.dances.entity.Trainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainerDto {

    private Long id;
    private String nameTrainer;
    private Long clubId;
    private String clubName;
    private Long categoryJudgeId;
    private Long organizationId;
    private Long cityId;
    private Long countryId;
    private String categoryJudgeName;
    private String organizationName;
    private String cityName;
    private String countryName;


    public static TrainerDto fromEntity(Trainer trainer) {

        TrainerDto dto = new TrainerDto();
        dto.setId(trainer.getId());
        dto.setNameTrainer(trainer.getNameTrainer());
        dto.setClubId(trainer.getClub() != null ? trainer.getClub().getId() : null);
        dto.setClubName(trainer.getClub() != null ? trainer.getClub().getNameClub() : null);
        dto.setCategoryJudgeId(trainer.getCategoryJudge() != null ? trainer.getCategoryJudge().getId() : null);
        dto.setCategoryJudgeName(trainer.getCategoryJudge() != null ? trainer.getCategoryJudge().getNameCategory() : null);
        dto.setOrganizationId(trainer.getOrganization() != null ? trainer.getOrganization().getId() : null);
        dto.setOrganizationName(trainer.getOrganization() != null ? trainer.getOrganization().getNameOrganization() : null);
        dto.setCityId(trainer.getCity() != null ? trainer.getCity().getId() : null);
        dto.setCityName(trainer.getCity() != null ? trainer.getCity().getNameCity() : null);
        dto.setCountryId(trainer.getCountry() != null ? trainer.getCountry().getId() : null);
        dto.setCountryName(trainer.getCountry() != null ? trainer.getCountry().getNameCountry() : null);

        return dto;

    }
}
