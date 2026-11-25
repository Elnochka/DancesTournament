package com.dances.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum GenderEnum {

    MALE,
    FEMALE;

    @JsonCreator
    public static GenderEnum fromString(String value) {
        if (value == null) return null;
        return GenderEnum.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
