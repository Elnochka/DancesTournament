package com.dances.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    @GetMapping("/{enumName}")
    public ResponseEntity<?> getEnumValues(@PathVariable String enumName) {

        try {
            String basePackage = "com.dances.model."; // свій пакет
            Class<?> enumClass = Class.forName(basePackage + enumName);

            Object[] values = enumClass.getEnumConstants();

            return ResponseEntity.ok(
                    Arrays.stream(values)
                            .map(Object::toString)
                            .toList()
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Enum not found: " + enumName);
        }
    }
}
