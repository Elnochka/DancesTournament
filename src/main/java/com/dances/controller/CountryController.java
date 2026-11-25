package com.dances.controller;

import com.dances.entity.Country;
import com.dances.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @Operation(summary = "Створити нову країну")
    @PostMapping
    public ResponseEntity<Country> createClub(@RequestBody Country country) {
        return ResponseEntity.ok(countryService.createCountry(country));
    }

    @Operation(summary = "Отримати список усіх країн")
    @GetMapping
    public ResponseEntity<List<Country>> getAllClubs() {
        return ResponseEntity.ok(countryService.getAllCountry());
    }

    @Operation(summary = "Отримати клуб за ID")
    @GetMapping("/{id}")
    public ResponseEntity<Country> getClubById(@PathVariable Long id) {
        return countryService.getCountryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Оновити країну")
    @PutMapping("/{id}")
    public ResponseEntity<Country> updateClub(@PathVariable Long id, @RequestBody Country country) {
        return ResponseEntity.ok(countryService.updateCountry(id, country));
    }

    @Operation(summary = "Видалити країну")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }
}
