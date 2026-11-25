package com.dances.service;

import com.dances.entity.dto.*;

import java.util.List;
import java.util.Optional;

public interface DanceService {

    List<DanceDto> getAll();
    Optional<DanceDto> getById(Long id);
    DanceDto create(DanceRequestDto dto);
    DanceDto patchUpdate(Long id, DancePatchDto dto);
    void deleteById(Long id);
    List<DanceDto> findByCategoryTournamentId(Long categoryId);
}
