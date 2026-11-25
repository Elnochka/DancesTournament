package com.dances.controller;

import com.dances.entity.Klass;
import com.dances.service.KlassService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/klasses")
@RequiredArgsConstructor
public class KlassController {

    private final KlassService klassService;

    @Operation(summary = "Створити нове клас")
    @PostMapping
    public ResponseEntity<Klass> createKlass(@RequestBody Klass klass) {
        return ResponseEntity.ok(klassService.createKlass(klass));
    }

    @Operation(summary = "Отримати список усіх класів")
    @GetMapping
    public ResponseEntity<List<Klass>> getAllKlasses() {
        return ResponseEntity.ok(klassService.getAllKlasses());
    }

    @Operation(summary = "Отримати клас за ID")
    @GetMapping("/{id}")
    public ResponseEntity<Klass> getKlassById(@PathVariable Long id) {
        return klassService.getKlassById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити клас")
    @PutMapping("/{id}")
    public ResponseEntity<Klass> updateKlass(@PathVariable Long id, @RequestBody Klass klass) {
        return ResponseEntity.ok(klassService.updateKlass(id, klass));
    }

    @Operation(summary = "Видалити клас")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKlass(@PathVariable Long id) {
        klassService.deleteKlass(id);
        return ResponseEntity.noContent().build();
    }
}
