package dev.ultreon.quantum.client.gui.debug;

import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.BlockPos;

public interface DebugPageContext {
    DebugPageContext left();

    DebugPageContext left(String text);

    DebugPageContext left(String key, Object value);

    DebugPageContext right();

    DebugPageContext right(String text);

    DebugPageContext right(String key, Object value);

    DebugPageContext entryLine(int idx, String name, long nanos);

    DebugPageContext entryLine(String name, String value);
    DebugPageContext entryLine(int idx, String name);

    DebugPageContext entryLine(TextObject value);

    DebugPageContext entryLine();

    QuantumClient client();

    default Vec3i block2sectionPos(BlockPos blockPos) {
        return new Vec3i(blockPos.x() / 16, blockPos.y() / 16, blockPos.z() / 16);
    }
}
