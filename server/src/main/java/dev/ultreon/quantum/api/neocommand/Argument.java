package dev.ultreon.quantum.api.neocommand;

public record Argument<T>(Class<? extends T> type, String name, T value) {
}
