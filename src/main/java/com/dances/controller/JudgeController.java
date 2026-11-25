package com.dances.controller;

import com.dances.entity.dto.JudgeDto;
import com.dances.entity.dto.JudgePatchDto;
import com.dances.entity.dto.JudgeRequestDto;
import com.dances.service.JudgeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/judges")
@RequiredArgsConstructor
public class JudgeController {

    private final JudgeService judgeService;

    @Operation(summary = "Створити нового суддю")
    @PostMapping
    public ResponseEntity<JudgeDto> create(@RequestBody JudgeRequestDto dto) {
        return ResponseEntity.ok(judgeService.create(dto));
    }

    @Operation(summary = "Отримати всіх суддів")
    @GetMapping
    public ResponseEntity<List<JudgeDto>> getAll() {
        return ResponseEntity.ok(judgeService.getAll());
    }

    @Operation(summary = "Отримати суддю за ID")
    @GetMapping("/{id}")
    public ResponseEntity<JudgeDto> getById(@PathVariable Long id) {
        return judgeService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити суддю за ID")
    @PatchMapping("/{id}")
    public ResponseEntity<JudgeDto> update(@PathVariable Long id, @RequestBody JudgePatchDto dto) {
        return ResponseEntity.ok(judgeService.patchUpdate(id, dto));
    }

    @Operation(summary = "Видалити суддю за ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        judgeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
