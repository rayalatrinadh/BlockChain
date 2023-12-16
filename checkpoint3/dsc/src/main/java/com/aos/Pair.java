package com.aos;

public class Pair<U, V> {
    private final U first;    // first field of a Pair
    private final V second;   // second field of a Pair

    // Constructs a new Pair with specified values
    private Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    // Factory method for creating a Typed Pair immutable instance
    public static <U, V> Pair <U, V> of(U a, V b) {
        // calls private constructor
        return new Pair<>(a, b);
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }
}
