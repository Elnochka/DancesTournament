package com.dances.controller;

import com.dances.entity.dto.SkatingResultDto;
import com.dances.service.impl.SkatingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skating")
@RequiredArgsConstructor
@Tag(name = "Skating", description = "Управління результатами по Skating System")
public class SkatingController {

    private final SkatingService skatingService;

    /**
     * Запустити розрахунок Skating System
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/final")
    public ResponseEntity<List<SkatingResultDto>> calculate(
            @RequestParam Long categoryId
    ) {

        List<SkatingResultDto> results = skatingService.calculateFinalPlaces(categoryId);
        return ResponseEntity.ok(results);
    }

    /**
     * Отримати вже розраховані результати
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/results")
    public ResponseEntity<List<SkatingResultDto>> getResults(
            @RequestParam Long categoryId
    ) {
        return ResponseEntity.ok(skatingService.getResults(categoryId));
    }

    @PostMapping("/move-next-round")
    @PreAuthorize("hasRole('ADMIN')")
    public void moveToNextRound(
            @RequestParam Long categoryId,
            @RequestParam Long roundSettingsId

    ) {
        skatingService.createMarksOrFinalMarksForCategoryAndRound(categoryId, roundSettingsId);
    }

    @PostMapping("/move-next-dance")
    @PreAuthorize("hasRole('ADMIN')")
    public void moveToNextDance(
            @RequestParam Long categoryId,
            @RequestParam Long roundSettingsId,
            @RequestParam Long danceId
    ) {
        skatingService.lockDance(categoryId, roundSettingsId, danceId);
    }

}
