package com.dances.controller;

import com.dances.entity.Users;
import com.dances.entity.dto.*;
import com.dances.model.RoleEnum;
import com.dances.model.RoundEnum;
import com.dances.service.MarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marks")
@RequiredArgsConstructor
@Tag(name = "Mark", description = "Управління оцінками")
public class MarkController {

    private final MarkService markService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Додати оцінку")
    @PostMapping
    public ResponseEntity<MarkDto> create(@RequestBody MarkRequestDto dto) {
        return ResponseEntity.ok(markService.create(dto));
    }

    @Operation(summary = "Отримати всі оцінки категорії + раунду")
    @GetMapping("/{categoryId}/{round}")
    public ResponseEntity<List<MarkDto>> getMarks(
            @PathVariable Long categoryId,
            @PathVariable String round
    ) {
        RoundEnum roundEnum = RoundEnum.fromString(round);
        return ResponseEntity.ok(markService.getMarks(categoryId, roundEnum));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Оновити оцінку за ID")
    @PatchMapping("/{id}")
    public ResponseEntity<MarkDto> update(@PathVariable Long id, @RequestBody MarkPatchDto dto) {
        return ResponseEntity.ok(markService.patchUpdate(id, dto));
    }

    @GetMapping
    @Operation(summary = "Отримати всі оцінки")
    public List<MarkDto> getMarksForJudge(@AuthenticationPrincipal Users user) {
        if(user.getRole().equals(RoleEnum.JUDGE)) {
            return markService.getMarksForJudgeName(user.getName());
        }
        return markService.getAll(); // для admin
    }

    @PreAuthorize("hasRole('JUDGE')")
    @PatchMapping("/{id}/passed")
    public MarkDto updatePassed(@PathVariable Long id, @RequestBody MarkPatchJudgeDto passed) {
        return markService.updatePassed(id, passed);
    }


}
