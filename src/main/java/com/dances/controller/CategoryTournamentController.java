package com.dances.controller;

import com.dances.entity.dto.CategoryTournamentDto;
import com.dances.entity.dto.CategoryTournamentRequestDto;
import com.dances.service.CategoryTournamentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-tournaments")
@RequiredArgsConstructor
public class CategoryTournamentController {
    private final CategoryTournamentService categoryTournamentService;

    @Operation(summary = "Отримати всі категорії турнірів")
    @GetMapping
    public ResponseEntity<List<CategoryTournamentDto>> getAll() {
        return ResponseEntity.ok(categoryTournamentService.getAll());
    }

    @Operation(summary = "Отримати категорію турніру за ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryTournamentDto> getById(@PathVariable Long id) {
        return categoryTournamentService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Створити нову категорію турніру")
    @PostMapping
    public ResponseEntity<CategoryTournamentDto> create(@RequestBody CategoryTournamentRequestDto dto) {
        return ResponseEntity.ok(categoryTournamentService.createCT(dto));
    }

    @Operation(summary = "Видалити категорію турніру")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryTournamentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
