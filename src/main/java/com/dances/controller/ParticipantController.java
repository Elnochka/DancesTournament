package com.dances.controller;

import com.dances.entity.dto.ParticipantDto;
import com.dances.entity.dto.ParticipantPatchDto;
import com.dances.entity.dto.ParticipantRequestDto;
import com.dances.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @Operation(summary = "Створити нового учасника")
    @PostMapping
    public ResponseEntity<ParticipantDto> create(@RequestBody ParticipantRequestDto dto) {
        return ResponseEntity.ok(participantService.createParticipant(dto));
    }

    @Operation(summary = "Отримати всіх учасників")
    @GetMapping
    public ResponseEntity<List<ParticipantDto>> getAll() {
        return ResponseEntity.ok(participantService.getAll());
    }

    @Operation(summary = "Отримати учасника за ID")
    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDto> getById(@PathVariable Long id) {
        return participantService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити учасника за ID")
    @PatchMapping("/{id}")
    public ResponseEntity<ParticipantDto> update(@PathVariable Long id, @RequestBody ParticipantPatchDto dto) {
        return ResponseEntity.ok(participantService.patchUpdate(id, dto));
    }

    @Operation(summary = "Видалити учасника за ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        participantService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
