package model.enums;

public enum UserRole {
    ADMIN,
    MEMBER;

    public static UserRole fromString(String role) {
        for (UserRole r : UserRole.values())
            if (r.name().equalsIgnoreCase(role.trim()))
                return r;
        throw new IllegalArgumentException("Invalid role: " + role);
    }

    @Override
    public String toString() {
        return name().toLowerCase(); // "admin", "member"
    }
}
