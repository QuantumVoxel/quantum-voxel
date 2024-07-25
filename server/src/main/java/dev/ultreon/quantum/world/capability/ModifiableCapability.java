package dev.ultreon.quantum.world.capability;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.Nullable;

public abstract class ModifiableCapability<Value> extends ArrayCapability<Value> {
    public ModifiableCapability(CapabilityType<? extends ModifiableCapability<Value>, Array<Value>> type, Class<? extends Value> clazz) {
        super(type, clazz);
    }

    public ModifiableCapability(CapabilityType<? extends ModifiableCapability<Value>, Array<Value>> type, Array<Value> entries, Class<? extends Value> clazz) {
        super(type, entries, clazz);
    }

    public ModifiableCapability(CapabilityType<? extends ModifiableCapability<Value>, Array<Value>> type, MapType mapType, Class<? extends Value> clazz) {
        super(type, mapType, clazz);
    }

    public void set(int index, @Nullable Value value) {
        this.entries.set(index, value);
        this.updated = true;
    }
}
