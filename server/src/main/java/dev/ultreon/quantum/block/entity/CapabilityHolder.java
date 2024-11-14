package dev.ultreon.quantum.block.entity;

import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.capability.Capability;
import dev.ultreon.quantum.world.capability.CapabilityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface CapabilityHolder {
    default <Instance extends Capability<Holder>, Holder> Optional<Instance> getCapability(@NotNull CapabilityType<Instance, Holder> capability) {
        return getCapability(capability, null);
    }

    default <Instance extends Capability<Holder>, Holder> Optional<Instance> getCapability(@NotNull CapabilityType<Instance, Holder> capability, @Nullable Direction direction) {
        return Optional.empty();
    }
}
