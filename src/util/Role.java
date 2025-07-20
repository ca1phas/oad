package util;

public enum Role {
    ADMIN,
    MEMBER;

    public static Role fromString(String role) {
        for (Role r : Role.values())
            if (r.name().equalsIgnoreCase(role.trim()))
                return r;
        throw new IllegalArgumentException("Invalid role: " + role);
    }

    @Override
    public String toString() {
        return name().toLowerCase(); // "admin", "member"
    }
}
