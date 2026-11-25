package com.dances.controller;

import com.dances.entity.dto.*;
import com.dances.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @Operation(summary = "Отримати тренера за ID (DTO)")
    @GetMapping("/{id}")
    public ResponseEntity<TrainerDto> getTrainerById(@PathVariable Long id) {
        return trainerService.getById(id)
                .map(trainer -> ResponseEntity.ok(TrainerDto.fromEntity(trainer)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Створити нового тренера")
    @PostMapping
    public ResponseEntity<TrainerDto> createTrainer(@RequestBody TrainerRequestDto dto) {
        return ResponseEntity.ok(trainerService.createTrainer(dto));
    }

    @Operation(summary = "Отримати всіх тренерів")
    @GetMapping
    public ResponseEntity<List<TrainerDto>> getAllTrainers() {
        return ResponseEntity.ok(trainerService.getAll());
    }

    @Operation(summary = "Оновити тренера за ID")
    @PatchMapping("/{id}")
    public ResponseEntity<TrainerDto> update(@PathVariable Long id, @RequestBody TrainerPatchDto dto) {
        return ResponseEntity.ok(trainerService.patchUpdate(id, dto));
    }

}
