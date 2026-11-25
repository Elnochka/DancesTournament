package com.dances.service.impl;

import com.dances.entity.KindOfDance;
import com.dances.entity.Participant;
import com.dances.entity.dto.ParticipantDto;
import com.dances.entity.dto.ParticipantPatchDto;
import com.dances.entity.dto.ParticipantRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.*;
import com.dances.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TrainerRepository trainerRepository;
    private final KlassRepository klassRepository;
    private final KindOfDanceRepository kindOfDanceRepository;
    private final CountryRepository countryRepository;

    @Override
    public ParticipantDto createParticipant(ParticipantRequestDto dto) {
        Participant p = dto.toEntity();
        p.setTrainer(trainerRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new NotFoundException("Trainer not found")));
        p.setKlass(klassRepository.findById(dto.getKlassId())
                .orElseThrow(() -> new NotFoundException("Klass not found")));
        p.setCountry(countryRepository.findById(dto.getCountryId())
                .orElseThrow(() -> new NotFoundException("Country not found")));
        Set<KindOfDance> dances = dto.getKindOfDanceIds().stream()
                .map(id -> kindOfDanceRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("KindOfDance " + id + " not found")))
                .collect(Collectors.toSet());
        p.setKindOfDances(dances);

        Participant saved = participantRepository.save(p);
        return ParticipantDto.fromEntity(saved);
    }

    @Override
    public List<ParticipantDto> getAll() {
        return participantRepository.findAll().stream()
                .map(ParticipantDto::fromEntity)
                .toList();
    }

    @Override
    public Optional<ParticipantDto> getById(Long id) {
        return participantRepository.findById(id).map(ParticipantDto::fromEntity);
    }

    @Override
    public void deleteById(Long id) {
        participantRepository.deleteById(id);
    }

    @Override
    public ParticipantDto patchUpdate(Long id, ParticipantPatchDto dto) {
        Participant existing = participantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Participant with id " + id + " not found"));

        if (dto.getFirstnameUkr() != null) existing.setFirstnameUkr(dto.getFirstnameUkr());
        if (dto.getLastnameUkr() != null) existing.setLastnameUkr(dto.getLastnameUkr());

        if (dto.getDateBirth() != null) existing.setDateBirth(dto.getDateBirth());
        if (dto.getGender() != null) existing.setGender(dto.getGender());
        if (dto.getFirstnameEn() != null) existing.setFirstnameEn(dto.getFirstnameEn());
        if (dto.getLastnameEn() != null) existing.setLastnameEn(dto.getLastnameEn());

        if (dto.getKlassId() != null) {

            existing.setKlass(klassRepository.findById(dto.getKlassId())
                    .orElseThrow(() -> new NotFoundException("Klass with id " + dto.getKlassId() + " not found")));
        }
        if (dto.getCountryId() != null) {

            existing.setCountry(countryRepository.findById(dto.getCountryId())
                    .orElseThrow(() -> new NotFoundException("Country with id " + dto.getCountryId() + " not found")));
        }

        if (dto.getKindOfDanceIds() != null) {

            Set<KindOfDance> dances = dto.getKindOfDanceIds().stream()
                    .map(ids -> kindOfDanceRepository.findById(ids)
                            .orElseThrow(() -> new NotFoundException("KindOfDance " + ids + " not found")))
                    .collect(Collectors.toSet());

            existing.setKindOfDances(dances);
        }

        Participant updated = participantRepository.save(existing);
        return ParticipantDto.fromEntity(updated);
    }

}
