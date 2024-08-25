package dev.ultreon.quantum.world.capability;

import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.energy.Energy;
import dev.ultreon.quantum.world.energy.EnergyNetwork;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class EnergyCapability implements Capability<Energy>, Comparable<EnergyCapability> {
    private final Energy data;
    private final CapabilityType<? extends EnergyCapability, Energy> type;

    public EnergyCapability(CapabilityType<? extends EnergyCapability, Energy> type, int capacity) {
        this.data = new Energy(capacity);
        this.type = type;
    }

    public EnergyCapability() {
        this.data = new Energy();
        this.type = null;
    }

    @Override
    public Energy get() {
        return data;
    }

    @Override
    public void save(MapType data) {
        this.data.save(data);
    }

    @Override
    public MapType load(World world, MapType data) {
        this.data.load(data);
        return data;
    }

    @Override
    public boolean isUpdated() {
        return data.isUpdated();
    }

    @Override
    public CapabilityType<? extends EnergyCapability, Energy> getType() {
        return type;
    }

    public void generate(int amount) {
        this.data.generate(amount);
    }

    public void consume(int amount) {
        this.data.consume(amount);
    }

    public void setEnergy(int energy) {
        this.data.setEnergy(energy);
    }

    public int getEnergy() {
        return this.data.getEnergy();
    }

    public void onRevalidate(EnergyNetwork energyNetwork) {
        // For implementations
    }

    @Override
    public int compareTo(EnergyCapability o) {
        return this.data.compareTo(o.data);
    }

    public int getTransferRate() {
        return this.data.getTransferRate();
    }

    public int getCapacity() {
        return this.data.getCapacity();
    }

    public void setCapacity(int capacity) {
        this.data.setCapacity(capacity);
    }

    public void setTransferRate(int transferRate) {
        this.data.setTransferRate(transferRate);
    }

    public boolean accepts(int amount) {
        return this.data.accepts(amount);
    }

    public boolean transfer(Energy energy, int amount) {
        return this.data.transfer(energy, amount);
    }

    public boolean transfer(EnergyCapability energy, int amount) {
        return this.data.transfer(energy.data, amount);
    }

    public boolean isFull() {
        return this.data.isFull();
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public void discharge() {
        this.data.discharge();
    }
}
