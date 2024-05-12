package dev.ultreon.quantum.client.config;

import dev.ultreon.quantum.client.gui.Screen;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ConfigScreenFactory {
    Screen create(@Nullable Screen back);
}
