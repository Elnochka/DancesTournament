package com.dances.service;

import com.dances.entity.FinalMark;
import com.dances.entity.ParticipantTournament;
import com.dances.entity.dto.*;

import java.util.List;
import java.util.Map;

public interface FinalMarkService {

    List<FinalMarkDto> getAll();
    List<FinalMarkDto> getByCategory(Long categoryId);
    FinalMarkDto create(FinalMarkRequestDto dto);
    void delete(Long id);
    FinalMarkDto patchUpdate(Long id, FinalMarkPatchDto dto);
    List<FinalMarkDto> getFinalMarksForJudgeName(String judgeName);
    FinalMarkDto updatePlace(Long id, FinalMarkPatchJudgeDto dto);
    void validateFinalMarks(Map<ParticipantTournament, List<FinalMark>> marksByParticipant, int judgesCount,
                       int participantCount);
    void validateSingleDanceFinalMarks(List<FinalMark> marks);
}
