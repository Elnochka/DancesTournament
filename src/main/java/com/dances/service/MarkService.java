package com.dances.service;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Judge;
import com.dances.entity.Mark;
import com.dances.entity.dto.*;
import com.dances.model.RoundEnum;

import java.util.List;

public interface MarkService {

    List<MarkDto> getAll();
    MarkDto create(MarkRequestDto dto);
    List<MarkDto> getMarks(Long categoryId, RoundEnum round);
//    List<ParticipantTournamentDto> calculatePassing(Long categoryId, RoundEnum round);
    MarkDto patchUpdate(Long id, MarkPatchDto dto);
    List<MarkDto> getMarksForJudgeName(String judgeName);
    MarkDto updatePassed(Long id, MarkPatchJudgeDto dto);
    void createMarks(CategoryTournament category, RoundEnum nextRound,
                     List<ParticipantTournamentDto> participants, List<Judge> judges, boolean social);
    void validateNonSocialMarks(List<Mark> marks);
//    void createMarksOrFinalMarksForCategoryAndRound(Long categoryId, RoundEnum round);

}
