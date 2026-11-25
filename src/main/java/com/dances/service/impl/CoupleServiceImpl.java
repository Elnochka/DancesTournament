package com.dances.service.impl;

import com.dances.entity.Couple;
import com.dances.entity.dto.CoupleDto;
import com.dances.entity.dto.CouplePatchDto;
import com.dances.entity.dto.CoupleRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.*;
import com.dances.service.CoupleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoupleServiceImpl implements CoupleService {

    private final CoupleRepository coupleRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public List<CoupleDto> getAll() {
        return coupleRepository.findAll().stream()
                .map(CoupleDto::fromEntity)
                .toList();
    }

    @Override
    public Optional<CoupleDto> getById(Long id) {
        return coupleRepository.findById(id).map(CoupleDto::fromEntity);
    }

    @Override
    public CoupleDto create(CoupleRequestDto dto) {
        Couple p = dto.toEntity();

        p.setParticipantMale(participantRepository.findById(dto.getParticipantMaleId())
                .orElseThrow(() -> new NotFoundException("Participant male not found")));
        p.setParticipantFemale(participantRepository.findById(dto.getParticipantFemaleId())
                .orElseThrow(() -> new NotFoundException("Participant female not found")));

        Couple saved = coupleRepository.save(p);
        return CoupleDto.fromEntity(saved);
    }

    @Override
    public CoupleDto patchUpdate(Long id, CouplePatchDto dto) {

        Couple existing = coupleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Couple with id " + id + " not found"));

        if (dto.getNameCouple() != null) existing.setNameCouple(dto.getNameCouple());

        Couple updated = coupleRepository.save(existing);

        return CoupleDto.fromEntity(updated);
    }

    @Override
    public void deleteById(Long id) {
        coupleRepository.deleteById(id);
    }
}
