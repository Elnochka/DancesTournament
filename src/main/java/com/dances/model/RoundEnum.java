package com.dances.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoundEnum {

    DEFAULT,
    QUALIFICATION,
    ROUND_OF_128,
    ROUND_OF_96,
    ROUND_OF_64,
    ROUND_OF_48,
    ROUND_OF_32,
    ROUND_OF_24,
    ROUND_OF_16,
    ROUND_OF_12,
    SIXTEEN_FINAL,
    QUARTER_FINAL,
    SEMI_FINAL,
    FINAL;

    @JsonCreator
    public static RoundEnum fromString(String value) {
        if (value == null) return null;
        return RoundEnum.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
