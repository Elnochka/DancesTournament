package com.dances.service;

import com.dances.entity.dto.JudgeDto;
import com.dances.entity.dto.JudgePatchDto;
import com.dances.entity.dto.JudgeRequestDto;

import java.util.List;
import java.util.Optional;

public interface JudgeService {

    List<JudgeDto> getAll();
    Optional<JudgeDto> getById(Long id);
    JudgeDto create(JudgeRequestDto dto);
    JudgeDto patchUpdate(Long id, JudgePatchDto dto);
    void deleteById(Long id);
}
