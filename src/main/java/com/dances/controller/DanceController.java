package com.dances.controller;

import com.dances.entity.dto.DanceDto;
import com.dances.entity.dto.DancePatchDto;
import com.dances.entity.dto.DanceRequestDto;
import com.dances.service.DanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dances")
@RequiredArgsConstructor
public class DanceController {

    private final DanceService danceService;

    @Operation(summary = "Створити новий танець")
    @PostMapping
    public ResponseEntity<DanceDto> create(@RequestBody DanceRequestDto dto) {
        return ResponseEntity.ok(danceService.create(dto));
    }

    @Operation(summary = "Отримати всі танці")
    @GetMapping
    public ResponseEntity<List<DanceDto>> getAll() {
        return ResponseEntity.ok(danceService.getAll());
    }

    @Operation(summary = "Отримати танець за ID")
    @GetMapping("/{id}")
    public ResponseEntity<DanceDto> getById(@PathVariable Long id) {
        return danceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити танець за ID")
    @PutMapping("/{id}")
    public ResponseEntity<DanceDto> update(@PathVariable Long id, @RequestBody DancePatchDto dto) {
        return ResponseEntity.ok(danceService.patchUpdate(id, dto));
    }

    @Operation(summary = "Видалити танець за ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        danceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(params = "categoryTournamentId")
    public List<DanceDto> getDancesByCategory(
            @RequestParam Long categoryTournamentId
    ) {
        return danceService.findByCategoryTournamentId(categoryTournamentId);
    }
}
