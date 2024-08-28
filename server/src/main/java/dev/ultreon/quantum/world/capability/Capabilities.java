package dev.ultreon.quantum.world.capability;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.energy.Energy;
import dev.ultreon.quantum.world.energy.EnergyConnection;
import dev.ultreon.quantum.world.energy.EnergyNode;
import org.jetbrains.annotations.ApiStatus;

import static dev.ultreon.quantum.CommonConstants.id;

@ApiStatus.Experimental
public class Capabilities {
    public static final CapabilityType<EnergyCapability, Energy> ENERGY = new CapabilityType<>(EnergyCapability::new);
    public static final CapabilityType<EnergyNode, EnergyConnection> ENERGY_NODE = new CapabilityType<>(EnergyNode::new);
    public static final CapabilityType<ItemStorageCapability, Array<ItemStack>> ITEM_STORAGE = new CapabilityType<>(ItemStorageCapability::new);

    static {
        Registries.CAPABILITY_TYPE.register(id("energy"), ENERGY);
        Registries.CAPABILITY_TYPE.register(id("energy_node"), ENERGY_NODE);
        Registries.CAPABILITY_TYPE.register(id("item_storage"), ITEM_STORAGE);
    }

    public static void init() {

    }
}
