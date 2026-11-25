package com.dances.controller;

import com.dances.entity.dto.ActiveDanceStateDto;
import com.dances.entity.dto.ActiveDanceStateRequestDto;
import com.dances.service.ActiveDanceStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/active-dance")
@RequiredArgsConstructor
public class ActiveDanceStateController {

    private final ActiveDanceStateService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ActiveDanceStateDto getCurrent() {
        return service.getCurrent();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ActiveDanceStateDto update(
            @RequestBody ActiveDanceStateRequestDto dto
    ) {
        return service.update(dto);
    }
}
