package com.dances.service.impl;

import com.dances.entity.Trainer;
import com.dances.entity.dto.TrainerDto;
import com.dances.entity.dto.TrainerPatchDto;
import com.dances.entity.dto.TrainerRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.*;
import com.dances.service.TrainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository trainerRepository;
    private final ClubRepository clubRepository;
    private final CategoryJudgeRepository categoryJudgeRepository;
    private final OrganizationRepository organizationRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;

    @Override
    public Optional<Trainer> getById(Long id) {
        return trainerRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        trainerRepository.deleteById(id);
    }

    @Override
    public TrainerDto patchUpdate(Long id, TrainerPatchDto dto) {
        Trainer existing = trainerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trainer with id " + id + " not found"));

        if (dto.getNameTrainer() != null) existing.setNameTrainer(dto.getNameTrainer());

        if (dto.getCountryId() != null) {
            existing.setCountry(countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new NotFoundException("Country with id " +
                            dto.getCountryId() + " not found")));
        }

        if (dto.getCategoryJudgeId() != null) {
            existing.setCategoryJudge(categoryJudgeRepository.findById(dto.getCategoryJudgeId())
                    .orElseThrow(() -> new NotFoundException("Category judge with id " +
                            dto.getCategoryJudgeId() + " not found")));
        }

        if (dto.getCityId() != null) {
            existing.setCity(cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new NotFoundException("City with id " +
                            dto.getCityId() + " not found")));
        }

        if (dto.getOrganizationId() != null) {
            existing.setOrganization(organizationRepository.findById(dto.getOrganizationId())
                    .orElseThrow(() -> new NotFoundException("Organization with id " +
                            dto.getOrganizationId() + " not found")));
        }

        Trainer updated = trainerRepository.save(existing);
        return TrainerDto.fromEntity(updated);
    }

    @Override
    public TrainerDto createTrainer(TrainerRequestDto dto) {
        Trainer trainer = dto.toEntity();

        trainer.setClub(clubRepository.findById(dto.getClubId())
                .orElseThrow(() -> new NotFoundException("Club not found")));
        trainer.setCategoryJudge(categoryJudgeRepository.findById(dto.getCategoryJudgeId()).orElse(null));
        trainer.setOrganization(organizationRepository.findById(dto.getOrganizationId()).orElse(null));
        trainer.setCity(cityRepository.findById(dto.getCityId())
                .orElseThrow(() -> new NotFoundException("City not found")));
        trainer.setCountry(countryRepository.findById(dto.getCountryId())
                .orElseThrow(() -> new NotFoundException("Country not found")));

        return TrainerDto.fromEntity(trainerRepository.save(trainer));
    }

    @Override
    public List<TrainerDto> getAll() {
        return trainerRepository.findAll().stream()
                .map(TrainerDto::fromEntity)
                .toList();
    }
}
