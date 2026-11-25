package com.dances.service;

import com.dances.entity.dto.SkatingResultDto;
import com.dances.entity.dto.SkatingResultRequestDto;

import java.util.List;

public interface SkatingResultsService {

    List<SkatingResultDto> getAll();
    List<SkatingResultDto> getByCategory(Long categoryId);
    SkatingResultDto create(SkatingResultRequestDto dto);
    void delete(Long id);
}
