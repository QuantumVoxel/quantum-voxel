package dev.ultreon.quantum.world.energy;

import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.world.capability.Capabilities;
import dev.ultreon.quantum.world.capability.EnergyCapability;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class EnergyConnection {
    private final BlockEntity blockEntity;
    private final EnergyCapability energy;

    public EnergyConnection(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.energy = blockEntity.getCapability(Capabilities.ENERGY).orElse(null);
    }

    public EnergyConnection(BlockEntity blockEntity, EnergyCapability energy) {
        this.blockEntity = blockEntity;
        this.energy = energy;
    }

    @ApiStatus.Internal
    public EnergyConnection() {
        this.blockEntity = null;
        this.energy = null;
    }

    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public @Nullable EnergyCapability getEnergy() {
        return this.energy;
    }

    public void onRevalidate(EnergyNetwork energyNetwork) {
        blockEntity.getCapability(Capabilities.ENERGY).ifPresent(energy -> energy.onRevalidate(energyNetwork));
    }
}
