package com.dances.controller;

import com.dances.entity.CategoryParticipant;
import com.dances.service.CategoryParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category-participants")
@RequiredArgsConstructor
public class CategoryParticipantController {

    private final CategoryParticipantService categoryParticipantService;

    @Operation(summary = "Створити категорію учасників")
    @PostMapping
    public ResponseEntity<CategoryParticipant> create(@RequestBody CategoryParticipant category) {
        return ResponseEntity.ok(categoryParticipantService.create(category));
    }

    @Operation(summary = "Отримати всі категорії учасників")
    @GetMapping
    public ResponseEntity<List<CategoryParticipant>> getAll() {
        return ResponseEntity.ok(categoryParticipantService.getAll());
    }

    @Operation(summary = "Отримати категорію учасників за ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryParticipant> getById(@PathVariable Long id) {
        return categoryParticipantService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити категорію учасників")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryParticipant> update(@PathVariable Long id, @RequestBody CategoryParticipant category) {
        return ResponseEntity.ok(categoryParticipantService.update(id, category));
    }

    @Operation(summary = "Видалити категорію учасників")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryParticipantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
