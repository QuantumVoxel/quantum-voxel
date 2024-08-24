package dev.ultreon.quantum.text;

import dev.ultreon.quantum.text.icon.FontIconMap;

public record FontIconPart(
        FontIconMap map,
        String icon
) implements TextPart {
}
