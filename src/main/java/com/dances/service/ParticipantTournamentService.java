package com.dances.service;

import com.dances.entity.CategoryTournament;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.RoundSettings;
import com.dances.entity.dto.*;
import com.dances.model.SoloCoupleEnum;

import java.util.List;
import java.util.Optional;

public interface ParticipantTournamentService {

    List<ParticipantTournamentDto> getAll();
    Optional<ParticipantTournamentDto> getById(Long id);
    List<ParticipantTournamentDto> getByCategory(Long categoryId);
    List<ParticipantTournamentDto> getByCategoryAndType(Long categoryId, SoloCoupleEnum type);
    ParticipantTournamentDto create(ParticipantTournamentRequestDto dto);
    void deleteById(Long id);
    ParticipantTournamentDto patchUpdate(Long id, ParticipantTournamentPatchDto dto);
    List<ParticipantTournamentDto> selectParticipants(CategoryTournament category, RoundSettings settings,
                                                      boolean social);
//    void createMarksForCategory(CategoryTournament category, ParticipantTournament pt);
}
