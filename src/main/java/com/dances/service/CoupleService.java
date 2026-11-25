package com.dances.service;

import com.dances.entity.dto.*;

import java.util.List;
import java.util.Optional;

public interface CoupleService {

    List<CoupleDto> getAll();
    Optional<CoupleDto> getById(Long id);
    CoupleDto create(CoupleRequestDto dto);
    CoupleDto patchUpdate(Long id, CouplePatchDto dto);
    void deleteById(Long id);
}
