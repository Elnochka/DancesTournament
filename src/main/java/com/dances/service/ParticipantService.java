package com.dances.service;

import com.dances.entity.dto.ParticipantDto;
import com.dances.entity.dto.ParticipantPatchDto;
import com.dances.entity.dto.ParticipantRequestDto;

import java.util.List;
import java.util.Optional;

public interface ParticipantService {

    List<ParticipantDto> getAll();
    Optional<ParticipantDto> getById(Long id);
    ParticipantDto createParticipant(ParticipantRequestDto dto);
    ParticipantDto patchUpdate(Long id, ParticipantPatchDto dto);
    void deleteById(Long id);
}
