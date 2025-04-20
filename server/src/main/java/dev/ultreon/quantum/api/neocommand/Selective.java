package dev.ultreon.quantum.api.neocommand;

public record Selective(String name, Selector<?>... selectors) {
}
