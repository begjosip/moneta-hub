package com.moneta.hub.moneta.model.enums;

public enum UserStatus {

    /**
     * User is verified and free to use application
     */
    ACTIVE,

    /**
     * User is created but it is not verified
     */
    PENDING_CONFIRMATION,

    /**
     * User actions are blocked by administrators
     */
    BLOCKED,

    /**
     * User account is deleted (deactivated)
     */
    DELETED;
}
