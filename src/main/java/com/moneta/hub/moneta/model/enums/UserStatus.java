package com.moneta.hub.moneta.model.enums;

import lombok.Getter;

@Getter
public enum UserStatus {

    /**
     * User is verified and free to use application
     */
    ACTIVE("ACTIVE"),

    /**
     * User is created but it is not verified
     */
    PENDING_CONFIRMATION("PENDING CONFIRMATION"),

    /**
     * User actions are blocked by administrators
     */
    BLOCKED("BLOCKED"),

    /**
     * User account is deleted (deactivated)
     */
    DELETED("DELETED");

    private final String name;

    UserStatus(String name) {
        this.name = name;
    }
}
