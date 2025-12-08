package org.nkcoder.user.domain.model;

/** User roles in the User bounded context. */
public enum UserRole {
    MEMBER,
    ADMIN;

    public String toAuthority() {
        return "ROLE_" + this.name();
    }
}
