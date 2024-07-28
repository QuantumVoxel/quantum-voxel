package dev.ultreon.quantum.text;

import dev.ultreon.quantum.text.icon.FontIconMap;
import dev.ultreon.quantum.util.Identifier;

public record FontIconPart(
        FontIconMap map,
        String icon
) implements TextPart {
}
