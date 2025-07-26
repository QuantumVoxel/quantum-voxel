package dev.ultreon.xeox.api;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IMod {
    String modId();
    String name();
    String version();
    String author();
    List<String> authors();
    String description();
    String website();
    String source();
    String issues();
    String license();
    @Nullable IFileSystem filesystem();
    List<String> mixinConfigs();
}
