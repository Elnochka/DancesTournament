package com.dances.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoleEnum {

    DANCER,
    TRAINER,
    JUDGE,
    ADMIN;

    @JsonCreator
    public static RoleEnum fromString(String value) {
        if (value == null) return null;
        return RoleEnum.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }

//    DANCER(Constants.ROLE_DANCER),
//    TRAINER(Constants.ROLE_TRAINER),
//    JUDGE(Constants.ROLE_JUDGE),
//    ADMIN(Constants.ROLE_ADMIN);
//
//    private static final RoleEnum[] ROLES = {DANCER, TRAINER, JUDGE};
//
//    @Getter
//    private String name;
//
//    @JsonCreator
//    public static RoleEnum of(@JsonProperty("role") String value) {
//        return Stream.of(ROLES)
//                .filter(roleEnum -> roleEnum.toString().equals(value))
//                .findFirst()
//                .orElseGet(() -> Stream.of(ROLES)
//                        .filter(roleEnum -> roleEnum.getName().equals(value))
//                        .findFirst()
//                        .orElse(null));
//    }
//
//    @JsonValue
//    public String toValue() {
//        return this.name();
//    }
//
//    public static final class Constants {
//
//        public static final String ROLE_DANCER = "Dancer";
//        public static final String ROLE_TRAINER = "Trainer";
//        public static final String ROLE_JUDGE = "Judge";
//        public static final String ROLE_ADMIN = "Admin";
//    }
}
