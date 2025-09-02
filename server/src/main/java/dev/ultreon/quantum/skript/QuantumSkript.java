package dev.ultreon.quantum.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.shadow.org.jetbrains.annotations.Nullable;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.baseskript.BaseSkript;
import dev.ultreon.baseskript.plugins.PluginDescriptionFile;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.api.events.ItemStackEvent;
import dev.ultreon.quantum.api.events.block.BlockChangeEvent;
import dev.ultreon.quantum.api.events.block.BlockEvent;
import dev.ultreon.quantum.api.events.entity.EntityEvent;
import dev.ultreon.quantum.api.events.world.WorldEvent;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.World;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class QuantumSkript implements QuantumSkriptPlugin {
    private static @Nullable QuantumSkript instance;
    private final Supplier<RegistryHandle> registryFactory;
    private SkriptAddon addon;
    private RegistryHandle registryHandle;

    public QuantumSkript(Supplier<RegistryHandle> registryFactory) {
        this.registryFactory = registryFactory;
        if (getClass() == QuantumSkript.class) {
            if (instance != null) {
                throw new IllegalStateException("QuantumSkript already initialized!");
            }
            instance = this;
        }
        BaseSkript.getPluginManager().registerPlugin(this);
    }

    public static @Nullable QuantumSkript instance() {
        return instance;
    }

    @Override
    public String getName() {
        return "QuantumSkript";
    }

    @Override
    public String getVersion0() {
        return CommonConstants.VERSION;
    }

    @Override
    public void onLoad() {
        CommonConstants.LOGGER.info("Loading " + getName() + " v" + getVersion0());
    }

    @Override
    public void onEnable() {
        this.addon = Skript.registerAddon(this);
        try {
            this.addon.loadClasses("dev.ultreon.quantum.skript",
                    "events",
                    "expressions",
                    "effects",
                    "sections",
                    "structures");
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load classes:", e);
        }

        this.registerEventValues();
        this.registerClasses();
    }

    private void registerClasses() {
        Classes.registerClass(new ClassInfo<>(Block.class, "block")
                .name("Block Type")
                .examples("block with id quantum:stone")
        );

        Classes.registerClass(new ClassInfo<>(Item.class, "item")
                .name("Item Type")
                .examples("item with id quantum:stone")
        );

        Classes.registerClass(new ClassInfo<>(ItemStack.class, "itemstack")
                .name("Item Stack")
                .examples("3 of item with id quantum:stone")
        );

        Classes.registerClass(new ClassInfo<>(BlockState.class, "blockstate")
                .name("Block State")
        );

        Classes.registerClass(new ClassInfo<>(World.class, "world")
                .name("World")
        );

        Classes.registerClass(new ClassInfo<>(NamespaceID.class, "namespace")
                .name("Namespace ID")
                .examples("quantum:world")
        );

        Classes.registerClass(new ClassInfo<>(Vector3.class, "vector3")
                .name("XYZ Vector")
                .examples("vector at 1.5, 3.0, 4.5"));

        Classes.registerClass(new ClassInfo<>(Vector2.class, "vector2")
                .name("XY Vector")
                .examples("vector at 1.5, 3.5"));

        Classes.registerClass(new ClassInfo<>(GridPoint3.class, "gridpoint3")
                .name("XYZ Grid Point")
                .examples("grid point at 1, 2, 3"));

        Classes.registerClass(new ClassInfo<>(GridPoint2.class, "gridpoint2")
                .name("XY Grid Point")
                .examples("grid point at 1, 2"));

        Classes.registerClass(new ClassInfo<>(Sound.class, "sound")
                .name("Sound"));

        Classes.registerClass(new ClassInfo<>(FileHandle.class, "filehandle")
                .name("File Handle")
                .examples("internal file at \"/hello.txt\""));
    }

    private void registerEventValues() {
        EventValues.registerEventValue(BlockEvent.class, Block.class, BlockEvent::getBlock, 0);
        EventValues.registerEventValue(BlockEvent.Use.class, Block.class, BlockEvent::getBlock, 0);
        EventValues.registerEventValue(BlockChangeEvent.Broken.class, Block.class, BlockEvent::getBlock, 0);
        EventValues.registerEventValue(BlockChangeEvent.Place.class, Block.class, BlockEvent::getBlock, 0);
        EventValues.registerEventValue(BlockChangeEvent.Set.class, Block.class, BlockEvent::getBlock, 0);
        EventValues.registerEventValue(BlockChangeEvent.AttemptPlace.class, Block.class, BlockEvent::getBlock, 0);
        EventValues.registerEventValue(BlockChangeEvent.AttemptBreak.class, Block.class, BlockEvent::getBlock, 0);
        EventValues.registerEventValue(BlockEvent.class, BlockState.class, BlockEvent::getState, 0);
        EventValues.registerEventValue(WorldEvent.class, World.class, WorldEvent::getWorld, 0);
        EventValues.registerEventValue(EntityEvent.class, Entity.class, EntityEvent::getEntity, 0);
        EventValues.registerEventValue(EntityEvent.class, World.class, EntityEvent::getWorld, 0);
        EventValues.registerEventValue(ItemStackEvent.class, ItemStack.class, ItemStackEvent::getItemStack, 0);
        EventValues.registerEventValue(ItemStackEvent.class, Item.class, arg -> arg.getItemStack().getItem(), 0);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public PluginDescriptionFile getDescription() {
        return new PluginDescriptionFile() {
            @Override
            public String getDescription() {
                return "Hello there :D";
            }

            @Override
            public String getName() {
                return QuantumSkript.this.getName();
            }

            @Override
            public String getVersion() {
                return getVersion0();
            }

            @Override
            public List<String> getDepend() {
                return List.of(
                        "Skript"
                );
            }

            @Override
            public List<String> getSoftDepend() {
                return List.of();
            }

            @Override
            public String getMain() {
                return "dev.ultreon.quantum.skript.QuantumSkript";
            }

            @Override
            public String getFullName() {
                return "QuantumSkript " + getVersion();
            }

            @Override
            public String getWebsite() {
                return "https://github.com/QuantumVoxel/quantum-voxel";
            }
        };
    }

    @Override
    public Class<?>[] getClasses(String basePackage, String[] subPackages) {
        return null;
    }

    public SkriptAddon getAddon() {
        return addon;
    }

    public RegistryHandle getRegistryHandle() {
        return registryHandle != null
                ? registryHandle
                : (this.registryHandle = registryFactory.get());
    }
}
