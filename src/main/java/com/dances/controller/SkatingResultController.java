package com.dances.controller;

import com.dances.entity.dto.SkatingResultDto;
import com.dances.service.SkatingResultsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skating-results")
@RequiredArgsConstructor
@Tag(name = "SkatingResults", description = "Управління результатами по Skating Result")
public class SkatingResultController {

    private final SkatingResultsService skatingResultsService;

    @GetMapping
    @Operation(summary = "Отримати всі результати Skating System")
    public List<SkatingResultDto> getAll() {
        return skatingResultsService.getAll();
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Отримати результати за категорією")
    public List<SkatingResultDto> getByCategory(@PathVariable Long categoryId) {
        return skatingResultsService.getByCategory(categoryId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити результат Skating System")
    public void delete(@PathVariable Long id) {
        skatingResultsService.delete(id);
    }
}
