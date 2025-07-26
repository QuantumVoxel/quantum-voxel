package dev.ultreon.xeox.impl;

import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IMod;

import java.util.List;

public record Mod(
        String modId,
        String name,
        String description,
        String author,
        String license,
        String version,
        String website,
        String source,
        String issues,
        List<String> authors,
        IFileSystem filesystem,
        List<String> mixinConfigs,
        List<EntryPoint> entrypoints,
        List<String> permissions
) implements IMod {
}
