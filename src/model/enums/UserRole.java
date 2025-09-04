package model.enums;

// Represents roles a user can have in the system
public enum UserRole {
    ADMIN, // Administrator with full permissions
    MEMBER; // Regular user with limited permissions

    // Convert a string to a UserRole (case-insensitive)
    public static UserRole fromString(String role) {
        for (UserRole r : UserRole.values())
            if (r.name().equalsIgnoreCase(role.trim()))
                return r;
        throw new IllegalArgumentException("Invalid role: " + role);
    }

    // Always return role in lowercase when printed
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
