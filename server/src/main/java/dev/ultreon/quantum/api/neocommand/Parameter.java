package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.neocommand.params.ArgumentType;

public record Parameter<T>(
        String name,
        ArgumentType<T> type
) {

}
