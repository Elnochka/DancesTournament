package com.dances.service;

import com.dances.entity.Trainer;
import com.dances.entity.dto.*;

import java.util.List;
import java.util.Optional;


public interface TrainerService {

    List<TrainerDto> getAll();
    Optional<Trainer> getById(Long id);
    TrainerDto createTrainer(TrainerRequestDto trainer);
    void deleteById(Long id);
    TrainerDto patchUpdate(Long id, TrainerPatchDto dto);
}
