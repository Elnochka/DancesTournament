package com.dances.service.impl;

import com.dances.entity.ActiveDanceState;
import com.dances.entity.dto.ActiveDanceStateDto;
import com.dances.entity.dto.ActiveDanceStateRequestDto;
import com.dances.repository.ActiveDanceStateRepository;
import com.dances.service.ActiveDanceStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActiveDanceStateServiceImpl implements ActiveDanceStateService {

    private static final Long STATE_ID = 1L;

    private final ActiveDanceStateRepository activeDanceStateRepository;

    @Override
    @Transactional(readOnly = true)
    public ActiveDanceStateDto getCurrent() {

        return activeDanceStateRepository.findById(STATE_ID)
                .map(ActiveDanceStateDto::fromEntity)
                .orElse(null);

    }

    @Override
    @Transactional
    public ActiveDanceStateDto update(ActiveDanceStateRequestDto dto) {

        ActiveDanceState state = activeDanceStateRepository.findById(STATE_ID)
                .orElseGet(() -> {
                    ActiveDanceState newState = new ActiveDanceState();
                    newState.setId(STATE_ID);
                    return newState;
                });

        state.setCategoryTournamentId(dto.getCategoryTournamentId());
        state.setRoundSettingsId(dto.getRoundSettingsId());
        state.setDanceId(dto.getDanceId());

        activeDanceStateRepository.save(state);

        return ActiveDanceStateDto.fromEntity(state);

    }
}
