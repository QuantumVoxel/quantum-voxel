package dev.ultreon.quantum.world.energy;

import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.capability.Capabilities;
import dev.ultreon.quantum.world.capability.Capability;
import dev.ultreon.quantum.world.capability.CapabilityType;
import dev.ultreon.quantum.world.capability.EnergyCapability;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class EnergyNode implements Capability<EnergyConnection>, Iterable<EnergyNode> {
    private EnergyConnection self = null;
    public @Nullable EnergyNode north = null;
    public @Nullable EnergyNode south = null;
    public @Nullable EnergyNode east = null;
    public @Nullable EnergyNode west = null;
    public @Nullable EnergyNode up = null;
    public @Nullable EnergyNode down = null;

    public EnergyNode(EnergyConnection self) {
        this.self = self;
    }

    @ApiStatus.Internal
    public EnergyNode() {

    }

    public EnergyConnection getSelf() {
        return self;
    }

    public void set(Direction connect, EnergyNode other) {
        switch (connect) {
            case NORTH:
                this.north = other;
                break;
            case SOUTH:
                this.south = other;
                break;
            case EAST:
                this.east = other;
                break;
            case WEST:
                this.west = other;
                break;
            case UP:
                this.up = other;
                break;
            case DOWN:
                this.down = other;
                break;
        }
    }

    @Override
    public CapabilityType<? extends Capability<EnergyConnection>, EnergyConnection> getType() {
        return Capabilities.ENERGY_NODE;
    }

    @Override
    public EnergyConnection get() {
        return null;
    }

    @Override
    public void save(MapType data) {
        data.put("Self", this.self.getBlockEntity().pos().save(new MapType()));
        if (this.north != null) data.put("North", this.north.self.getBlockEntity().pos().save(new MapType()));
        if (this.south != null) data.put("South", this.south.self.getBlockEntity().pos().save(new MapType()));
        if (this.east != null) data.put("East", this.east.self.getBlockEntity().pos().save(new MapType()));
        if (this.west != null) data.put("West", this.west.self.getBlockEntity().pos().save(new MapType()));
        if (this.up != null) data.put("Up", this.up.self.getBlockEntity().pos().save(new MapType()));
        if (this.down != null) data.put("Down", this.down.self.getBlockEntity().pos().save(new MapType()));
    }

    @Override
    public MapType load(World world, MapType data) {
        this.self = new EnergyConnection(world.getBlockEntity(new BlockVec(data.getMap("Self"))));
        if (data.<MapType>contains("North")) this.north = new EnergyNode(new EnergyConnection(world.getBlockEntity(new BlockVec(data.getMap("North")))));
        if (data.<MapType>contains("South")) this.south = new EnergyNode(new EnergyConnection(world.getBlockEntity(new BlockVec(data.getMap("South")))));
        if (data.<MapType>contains("East")) this.east = new EnergyNode(new EnergyConnection(world.getBlockEntity(new BlockVec(data.getMap("East")))));
        if (data.<MapType>contains("West")) this.west = new EnergyNode(new EnergyConnection(world.getBlockEntity(new BlockVec(data.getMap("West")))));
        if (data.<MapType>contains("Up")) this.up = new EnergyNode(new EnergyConnection(world.getBlockEntity(new BlockVec(data.getMap("Up")))));
        if (data.<MapType>contains("Down")) this.down = new EnergyNode(new EnergyConnection(world.getBlockEntity(new BlockVec(data.getMap("Down")))));
        return data;
    }

    @Override
    public boolean isUpdated() {
        return false;
    }

    public @NotNull Iterator<EnergyNode> iterator() {
        return Arrays.asList(north, south, east, west, up, down).stream().filter(Objects::nonNull).iterator();
    }

    public void onRevalidate(EnergyNetwork energyNetwork) {
        this.self.onRevalidate(energyNetwork);
    }

    public void tick() {
        // Transfer energy to lower neighbors
        transferIfLower(this.south);
        transferIfLower(this.north);
        transferIfLower(this.west);
        transferIfLower(this.east);
        transferIfLower(this.down);
        transferIfLower(this.up);
    }

    private void transferIfLower(EnergyNode other) {
        if (other != null) {
            EnergyCapability energy = other.getSelf().getEnergy();
            EnergyCapability self = this.getSelf().getEnergy();
            if (energy != null && self != null && self.compareTo(energy) > 0) {
                self.get().transfer(energy.get(), Math.max(self.getTransferRate(), energy.getTransferRate()));
            }
        }
    }
}
