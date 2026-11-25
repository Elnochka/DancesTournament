package com.dances.service;

import com.dances.entity.dto.CategoryTournamentDto;
import com.dances.entity.dto.CategoryTournamentRequestDto;

import java.util.List;
import java.util.Optional;

public interface CategoryTournamentService {

    List<CategoryTournamentDto> getAll();
    Optional<CategoryTournamentDto> getById(Long id);
    CategoryTournamentDto createCT(CategoryTournamentRequestDto categoryTournament);
//    CategoryTournament createFromDto(CategoryTournamentDto dto);
    void delete(Long id);
}
