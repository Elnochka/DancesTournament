package com.dances.controller;

import com.dances.entity.dto.*;
import com.dances.model.SoloCoupleEnum;
import com.dances.service.ParticipantTournamentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participant-tournaments")
@RequiredArgsConstructor
public class ParticipantTournamentController {

    private final ParticipantTournamentService participantTournamentService;

    @Operation(summary = "Отримати усіx учасників турніру")
    @GetMapping
    public ResponseEntity<List<ParticipantTournamentDto>> getAll() {
        return ResponseEntity.ok(participantTournamentService.getAll());
    }

    @Operation(summary = "Отримати учасника турніру за ID")
    @GetMapping("/{id}")
    public ResponseEntity<ParticipantTournamentDto> getById(@PathVariable Long id) {
        return participantTournamentService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ParticipantTournamentDto> create(
            @RequestBody ParticipantTournamentRequestDto dto) {
        return ResponseEntity.ok(participantTournamentService.create(dto));
    }

    @Operation(summary = "Оновити учасника турніру за ID")
    @PatchMapping("/{id}")
    public ResponseEntity<ParticipantTournamentDto> update(@PathVariable Long id, @RequestBody ParticipantTournamentPatchDto dto) {
        return ResponseEntity.ok(participantTournamentService.patchUpdate(id, dto));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ParticipantTournamentDto>> getByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(participantTournamentService.getByCategory(categoryId));
    }

    @GetMapping("/category/{categoryId}/{type}")
    public ResponseEntity<List<ParticipantTournamentDto>> getByCategoryAndType(
            @PathVariable Long categoryId,
            @PathVariable SoloCoupleEnum type) {
        return ResponseEntity.ok(participantTournamentService.getByCategoryAndType(categoryId, type));
    }

    @Operation(summary = "Видалити учасника турніру")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        participantTournamentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
