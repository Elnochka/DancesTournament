package com.dances.service.impl;

import com.dances.entity.CategoryTournament;
import com.dances.entity.dto.CategoryTournamentDto;
import com.dances.entity.dto.CategoryTournamentRequestDto;
import com.dances.exception.NotFoundException;
import com.dances.repository.CategoryParticipantRepository;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.KindOfDanceRepository;
import com.dances.repository.KlassRepository;
import com.dances.service.CategoryTournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryTournamentServiceImpl implements CategoryTournamentService {

    private final CategoryTournamentRepository categoryTournamentRepository;
    private final KlassRepository klassRepository;
    private final CategoryParticipantRepository categoryParticipantRepository;
    private final KindOfDanceRepository kindOfDanceRepository;

    @Override
    public CategoryTournamentDto createCT(CategoryTournamentRequestDto dto) {

        CategoryTournament categoryTournament = dto.toEntity();
        categoryTournament.setCategoryParticipant(categoryParticipantRepository.findById(dto.getCategoryParticipantId())
                .orElseThrow(() -> new NotFoundException("Category participant not found")));
        categoryTournament.setKlass(klassRepository.findById(dto.getKlassId())
                .orElseThrow(() -> new NotFoundException("Klass not found")));
        categoryTournament.setKindOfDance(kindOfDanceRepository.findById(dto.getKindOfDanceId())
                .orElseThrow(() -> new NotFoundException("Kind of dance not found")));

        CategoryTournament saved = categoryTournamentRepository.save(categoryTournament);
        return CategoryTournamentDto.fromEntity(saved);
    }

    @Override
    public List<CategoryTournamentDto> getAll() {

        return categoryTournamentRepository.findAll().stream()
                .map(CategoryTournamentDto::fromEntity)
                .toList();
    }

    @Override
    public Optional<CategoryTournamentDto> getById(Long id) {

        return categoryTournamentRepository.findById(id).map(CategoryTournamentDto::fromEntity);
    }

    @Override
    public void delete(Long id) {

        categoryTournamentRepository.deleteById(id);
    }
}
