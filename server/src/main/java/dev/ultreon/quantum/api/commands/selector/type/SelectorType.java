package dev.ultreon.quantum.api.commands.selector.type;

import org.jetbrains.annotations.Nullable;

@Deprecated
public abstract class SelectorType {
  private final @Nullable Object value;

  public SelectorType(@Nullable Object value) {
    this.value = value;
  }

  public @Nullable Object getValue() {
    return this.value;
  }
}