package dev.ultreon.quantum.client.gui.debug;

import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.world.vec.BlockVec;

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

    default Vec3i block2sectionPos(BlockVec blockVec) {
        return new Vec3i(blockVec.getIntX() / 16, blockVec.getIntY() / 16, blockVec.getIntZ() / 16);
    }
}
