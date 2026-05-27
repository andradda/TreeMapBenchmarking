package dk.itu.thesis.api;

public enum TreeOperation {
    READ,
    INSERT,
    DELETE,
    UPDATE,
    SNAPSHOT // Create and return a consistent read-only view of the tree at that moment
}
