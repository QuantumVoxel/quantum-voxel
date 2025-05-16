package dev.ultreon.quantum.world.capability;

import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface Capability<Value> {
    CapabilityType<? extends Capability<Value>, Value> getType();

    Value get();

    void save(MapType data);

    @Deprecated
    default MapType load(MapType data) {
        return data;
    }

    default MapType load(World world, MapType data) {
        return this.load(data);
    }

    boolean isUpdated();

    default void tick() {

    }
}
