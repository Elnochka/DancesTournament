package com.dances.service;

import com.dances.entity.Club;
import com.dances.entity.dto.*;
import com.dances.model.RoundEnum;

import java.util.List;
import java.util.Optional;

public interface RoundSettingsService {

    RoundSettingsDto getSettings(Long categoryId, RoundEnum round);

    RoundSettingsDto save(Long categoryId, RoundEnum round, Integer couplesToNextRound);

    int getCouplesToNextRound(Long categoryId, RoundEnum round);

    List<RoundSettingsDto> getAll();
    List<RoundSettingsDto> getRounds(Long categoryId);
    Optional<RoundSettingsDto> getById(Long id);
    RoundSettingsDto createRoundSettings(RoundSettingsRequestDto dto);
    RoundSettingsDto patchUpdate(Long id, RoundSettingsDto dto);
    void deleteById(Long id);
}
