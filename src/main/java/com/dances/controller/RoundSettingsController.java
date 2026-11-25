package com.dances.controller;

import com.dances.entity.dto.ParticipantDto;
import com.dances.entity.dto.ParticipantPatchDto;
import com.dances.entity.dto.RoundSettingsDto;
import com.dances.entity.dto.RoundSettingsRequestDto;
import com.dances.model.RoundEnum;
import com.dances.service.impl.RoundSettingsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/round-settings")
@RequiredArgsConstructor
public class RoundSettingsController {

    private final RoundSettingsServiceImpl roundSettingsService;

    @GetMapping(params = {"categoryTournamentId", "round"})
    public RoundSettingsDto getSettings(
            @RequestParam Long categoryTournamentId,
            @RequestParam RoundEnum round
    ) {
        return roundSettingsService.getSettings(
                categoryTournamentId,
                round
        );
    }

    @PutMapping
    public RoundSettingsDto saveSettings(
            @RequestParam Long categoryTournamentId,
            @RequestParam RoundEnum round,
            @RequestParam Integer couplesToNextRound
    ) {
        return roundSettingsService.save(
                categoryTournamentId,
                round,
                couplesToNextRound
        );
    }

    @GetMapping(params = "categoryTournamentId")
    public List<RoundSettingsDto> getRounds(@RequestParam Long categoryTournamentId) {
        return roundSettingsService.getRounds(categoryTournamentId);
    }

    @Operation(summary = "Створити новий раунд")
    @PostMapping
    public ResponseEntity<RoundSettingsDto> create(@RequestBody RoundSettingsRequestDto dto) {
        return ResponseEntity.ok(roundSettingsService.createRoundSettings(dto));
    }

    @Operation(summary = "Отримати список усіх раундів ")
    @GetMapping
    public ResponseEntity<List<RoundSettingsDto>> getAll() {
        return ResponseEntity.ok(roundSettingsService.getAll());
    }

    @Operation(summary = "Отримати раунд за ID")
    @GetMapping("/{id}")
    public ResponseEntity<RoundSettingsDto> getById(@PathVariable Long id) {
        return roundSettingsService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

//    @Operation(summary = "Оновити раунд за ID")
//    @PatchMapping("/{id}")
//    public ResponseEntity<RoundSettingsDto> update(@PathVariable Long id, @RequestBody RoundSettingsDto dto) {
//        return ResponseEntity.ok(roundSettingsService.patchUpdate(id, dto));
//    }

    @Operation(summary = "Оновити раунд")
    @PatchMapping("/{id}")
    public ResponseEntity<RoundSettingsDto> patchUpdate(@PathVariable Long id, @RequestBody RoundSettingsDto dto) {
        return ResponseEntity.ok(roundSettingsService.patchUpdate(id, dto));
    }

    @Operation(summary = "Видалити раунд")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        roundSettingsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
