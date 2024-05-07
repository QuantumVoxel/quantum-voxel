package dev.ultreon.quantum.client.model.blockbench;

import java.util.UUID;

public sealed interface BBModelOutlineInfo permits BBModelElementReference, BBModelGroup {
    UUID uuid();
}
