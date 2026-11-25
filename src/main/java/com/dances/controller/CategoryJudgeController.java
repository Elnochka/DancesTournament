package com.dances.controller;

import com.dances.entity.CategoryJudge;
import com.dances.service.CategoryJudgeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-judges")
@RequiredArgsConstructor
public class CategoryJudgeController {

    private final CategoryJudgeService categoryJudgeService;

    @Operation(summary = "Створити категорію суддів")
    @PostMapping
    public ResponseEntity<CategoryJudge> create(@RequestBody CategoryJudge categoryJudge) {
        return ResponseEntity.ok(categoryJudgeService.create(categoryJudge));
    }

    @Operation(summary = "Отримати всі категорії суддів")
    @GetMapping
    public ResponseEntity<List<CategoryJudge>> getAll() {

        return ResponseEntity.ok(categoryJudgeService.getAll());
    }

    @Operation(summary = "Отримати категорію суддів за ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryJudge> getById(@PathVariable Long id) {
        return categoryJudgeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити категорію суддів")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryJudge> update(@PathVariable Long id,
                                                @RequestBody CategoryJudge categoryJudge) {
        return ResponseEntity.ok(categoryJudgeService.update(id, categoryJudge));
    }

    @Operation(summary = "Видалити категорію суддів")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryJudgeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
