package com.dances.service.impl;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Dance;
import com.dances.entity.dto.DanceDto;
import com.dances.entity.dto.DancePatchDto;
import com.dances.entity.dto.DanceRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.DanceRepository;
import com.dances.repository.KindOfDanceRepository;
import com.dances.service.DanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DanceServiceImpl implements DanceService {

    private final DanceRepository danceRepository;
    private final KindOfDanceRepository kindOfDanceRepository;
    private final CategoryTournamentRepository categoryTournamentRepository;

    @Override
    public List<DanceDto> getAll() {
        return danceRepository.findAll().stream()
                .map(DanceDto::fromEntity)
                .toList();
    }

    @Override
    public Optional<DanceDto> getById(Long id) {

        return danceRepository.findById(id).map(DanceDto::fromEntity);
    }

    @Override
    public DanceDto create(DanceRequestDto dto) {
        Dance p = dto.toEntity();
        p.setKindOfDance(kindOfDanceRepository.findById(dto.getKindOfDanceId())
                .orElseThrow(() -> new NotFoundException("Kind of dance not found")));

        Dance saved = danceRepository.save(p);
        return DanceDto.fromEntity(saved);
    }

    @Override
    public DanceDto patchUpdate(Long id, DancePatchDto dto) {
        Dance existing = danceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dance with id " + id + " not found"));

        if (dto.getNameDance() != null) existing.setNameDance(dto.getNameDance());
        if (dto.getFullNameDance() != null) existing.setFullNameDance(dto.getFullNameDance());

        if (dto.getKindOfDanceId() != null) {
            existing.setKindOfDance(kindOfDanceRepository.findById(dto.getKindOfDanceId())
                    .orElseThrow(() -> new NotFoundException("Kind of dance with id " +
                            dto.getKindOfDanceId() + " not found")));
        }

        Dance updated = danceRepository.save(existing);
        return DanceDto.fromEntity(updated);
    }

    @Override
    public void deleteById(Long id) {
        danceRepository.deleteById(id);
    }

    @Override
    public List<DanceDto> findByCategoryTournamentId(Long categoryId) {
        log.info("Loading dances for categoryId={}", categoryId);
        CategoryTournament category = categoryTournamentRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "CategoryTournament not found: " + categoryId
                        )
                );

        Long kindOfDanceId = category.getKindOfDance().getId();

        return danceRepository.findByKindOfDanceId(kindOfDanceId)
                .stream()
                .map(DanceDto::fromEntity)
                .toList();
    }
}
