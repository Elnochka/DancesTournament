package com.dances.controller;

import com.dances.entity.dto.CoupleDto;
import com.dances.entity.dto.CouplePatchDto;
import com.dances.entity.dto.CoupleRequestDto;
import com.dances.service.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/couples")
@RequiredArgsConstructor
public class CoupleController {

    private final CoupleService coupleService;

    @Operation(summary = "Створити нову пару")
    @PostMapping
    public ResponseEntity<CoupleDto> create(@RequestBody CoupleRequestDto dto) {
        return ResponseEntity.ok(coupleService.create(dto));
    }

    @Operation(summary = "Отримати всі пари")
    @GetMapping
    public ResponseEntity<List<CoupleDto>> getAll() {
        return ResponseEntity.ok(coupleService.getAll());
    }

    @Operation(summary = "Отримати пару за ID")
    @GetMapping("/{id}")
    public ResponseEntity<CoupleDto> getById(@PathVariable Long id) {
        return coupleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити пару за ID")
    @PatchMapping("/{id}")
    public ResponseEntity<CoupleDto> update(@PathVariable Long id, @RequestBody CouplePatchDto dto) {
        return ResponseEntity.ok(coupleService.patchUpdate(id, dto));
    }

    @Operation(summary = "Видалити пару за ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coupleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
