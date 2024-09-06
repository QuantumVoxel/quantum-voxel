package dev.ultreon.quantum.text;

import dev.ultreon.quantum.api.commands.CommandSender;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TextKey {
    String get(@Nullable CommandSender sender);
}
