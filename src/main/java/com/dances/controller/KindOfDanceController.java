package com.dances.controller;

import com.dances.entity.KindOfDance;
import com.dances.service.KindOfDanceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kind-of-dances")
@RequiredArgsConstructor
public class KindOfDanceController {

    private final KindOfDanceService kindOfDanceService;

    @Operation(summary = "Створити новий вид танцю")
    @PostMapping
    public ResponseEntity<KindOfDance> create(@RequestBody KindOfDance kindOfDance) {
        return ResponseEntity.ok(kindOfDanceService.create(kindOfDance));
    }

    @Operation(summary = "Отримати список усіх видів танцю")
    @GetMapping
    public ResponseEntity<List<KindOfDance>> getAll() {

        return ResponseEntity.ok(kindOfDanceService.getAll());
    }

    @Operation(summary = "Отримати вид танцю за ID")
    @GetMapping("/{id}")
    public ResponseEntity<KindOfDance> getById(@PathVariable Long id) {
        return kindOfDanceService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити вид танцю")
    @PutMapping("/{id}")
    public ResponseEntity<KindOfDance> update(@PathVariable Long id, @RequestBody KindOfDance kindOfDance) {
        return ResponseEntity.ok(kindOfDanceService.update(id, kindOfDance));
    }

    @Operation(summary = "Видалити вид танцю")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        kindOfDanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
