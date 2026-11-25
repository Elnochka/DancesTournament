package com.dances.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SoloCoupleEnum {
    SOLO,
    COUPLE;

    @JsonCreator
    public static SoloCoupleEnum fromString(String value) {
        if (value == null) return null;
        return SoloCoupleEnum.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
