package model.base;

// Ensures each model provides a unique key (used by repositories)
public interface Identifiable {
    // Returns the unique key as a String
    String getKey();
}
