package com.dances.controller;

import com.dances.entity.Users;
import com.dances.entity.dto.*;
import com.dances.model.RoleEnum;
import com.dances.service.FinalMarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/final-marks")
@RequiredArgsConstructor
@Tag(name = "FinalMark", description = "Управління фінальними оцінками")
public class FinalMarkController {

    private final FinalMarkService finalMarkService;


    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Отримати фінальні оцінки за категорією")
    public List<FinalMarkDto> getByCategory(@PathVariable Long categoryId) {
        return finalMarkService.getByCategory(categoryId);
    }

    @GetMapping
    @Operation(summary = "Отримати всі фінальні оцінки")
    public List<FinalMarkDto> getMarksForJudge(@AuthenticationPrincipal Users user) {
        if(user.getRole().equals(RoleEnum.JUDGE)) {
            return finalMarkService.getFinalMarksForJudgeName(user.getName());
        }
        return finalMarkService.getAll(); // для admin
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Створити фінальну оцінку")
    public FinalMarkDto create(@RequestBody FinalMarkRequestDto dto) {
        return finalMarkService.create(dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити фінальну оцінку")
    public void delete(@PathVariable Long id) {
        finalMarkService.delete(id);
    }

    @Operation(summary = "Оновити фінальну оцінку за ID")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<FinalMarkDto> update(@PathVariable Long id, @RequestBody FinalMarkPatchDto dto) {
        return ResponseEntity.ok(finalMarkService.patchUpdate(id, dto));
    }

    @PreAuthorize("hasRole('JUDGE')")
    @PatchMapping("/{id}/place")
    public FinalMarkDto updatePassed(@PathVariable Long id, @RequestBody FinalMarkPatchJudgeDto place) {
        return finalMarkService.updatePlace(id, place);
    }
}
