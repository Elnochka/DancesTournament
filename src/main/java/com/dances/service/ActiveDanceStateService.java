package com.dances.service;

import com.dances.entity.dto.ActiveDanceStateDto;
import com.dances.entity.dto.ActiveDanceStateRequestDto;

public interface ActiveDanceStateService {

    ActiveDanceStateDto getCurrent();
    ActiveDanceStateDto update(ActiveDanceStateRequestDto dto);
}
