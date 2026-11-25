package com.dances.service.impl;

import com.dances.entity.SkatingResult;
import com.dances.entity.dto.SkatingResultDto;
import com.dances.entity.dto.SkatingResultRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.ParticipantTournamentRepository;
import com.dances.repository.SkatingResultsRepository;
import com.dances.service.SkatingResultsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkatingResultsServiceImpl implements SkatingResultsService {

    private final SkatingResultsRepository skatingResultsRepository;
    private final ParticipantTournamentRepository participantRepository;
    private final CategoryTournamentRepository categoryRepository;

    @Override
    public List<SkatingResultDto> getAll() {

        List<SkatingResultDto> result = skatingResultsRepository.findAll()
                .stream()
                .map(SkatingResultDto::fromEntity)
                .toList();

        log.info("Got {} skating results", result.size());

        return result;

    }

    @Override
    public List<SkatingResultDto> getByCategory(Long categoryId) {

        return skatingResultsRepository.findByCategoryTournamentId(categoryId)
                .stream()
                .map(SkatingResultDto::fromEntity)
                .toList();

    }

    @Override
    public SkatingResultDto create(SkatingResultRequestDto dto) {

        SkatingResult skatingResult = dto.toEntity();
        skatingResult.setParticipantTournament(participantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new NotFoundException("Participant not found")));

        skatingResult.setCategoryTournament(categoryRepository.findById(dto.getCategoryTournamentId())
                .orElseThrow(() -> new NotFoundException("Category not found")));

        skatingResult.setFinalPlace(dto.getFinalPlace());

        SkatingResult saved = skatingResultsRepository.save(skatingResult);

        log.info(
                "Skating result created: id={}, participant={}, category={}, place={}",
                saved.getId(),
                dto.getParticipantId(),
                dto.getCategoryTournamentId(),
                dto.getFinalPlace()
        );

        return SkatingResultDto.fromEntity(saved);

    }

    @Override
    public void delete(Long id) {

        skatingResultsRepository.deleteById(id);

        log.info("Skating result deleted: id={}", id);
    }
}
