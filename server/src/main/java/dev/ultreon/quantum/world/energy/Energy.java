package dev.ultreon.quantum.world.energy;

import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

public class Energy implements Comparable<Energy> {
    private int energy;
    private int capacity;
    private boolean updated;
    private int transferRate = 10;

    public Energy(int capacity) {
        this(0, capacity);
    }

    public Energy(int energy, int capacity) {
        this.energy = energy;
        this.capacity = capacity;
    }

    public Energy(int energy, int capacity, int transferRate) {
        this.energy = energy;
        this.capacity = capacity;
        this.transferRate = transferRate;
    }

    public Energy(MapType data) {
        this(data.getInt("energy"), data.getInt("capacity"));
    }

    public Energy() {
        this(0, 0);
    }

    public int getEnergy() {
        return this.energy;
    }

    public void setEnergy(int energy) {
        if (energy < 0) energy = 0;
        if (energy > this.capacity) energy = this.capacity;
        this.energy = energy;
        this.updated = true;
    }

    public void generate(int amount) {
        this.energy = Math.min(this.energy + amount, this.capacity);
        this.updated = true;
    }

    public void consume(int amount) {
        this.energy = Math.max(this.energy - amount, 0);
        this.updated = true;
    }

    public boolean isFull() {
        return this.energy == this.capacity;
    }

    public boolean isEmpty() {
        return this.energy == 0;
    }

    public void discharge() {
        this.energy = 0;
        this.updated = true;
    }

    public boolean transfer(Energy energy, int amount) {
        if (!energy.accepts(amount)) {
            return false;
        }

        energy.energy = Math.min(energy.energy + amount, energy.capacity);
        this.energy = Math.max(this.energy - amount, 0);

        energy.updated = true;
        this.updated = true;
        return true;
    }

    public boolean accepts(int amount) {
        return this.energy + amount > this.capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void save(MapType data) {
        data.putInt("energy", this.energy);
        data.putInt("capacity", this.capacity);
    }

    public void load(MapType data) {
        this.capacity = data.getInt("capacity");
        this.energy = data.getInt("energy");
        this.transferRate = data.getInt("transferRate");
        if (this.transferRate < 1) this.transferRate = 1;
        if (this.energy < 0) this.energy = 0;
        if (this.energy > this.capacity) this.energy = this.capacity;
        this.updated = true;
    }

    public boolean isUpdated() {
        return updated;
    }

    @Override
    public int compareTo(@NotNull Energy o) {
        return Integer.compare(this.energy, o.energy);
    }

    public int getTransferRate() {
        return transferRate;
    }

    public void setTransferRate(int transferRate) {
        this.transferRate = transferRate;
        if (this.transferRate < 1) this.transferRate = 1;

        this.updated = true;
    }
}
