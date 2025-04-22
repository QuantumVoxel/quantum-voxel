package dev.ultreon.quantum.client;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.SlabBlock;
import dev.ultreon.quantum.client.api.events.ClientRegistrationEvents;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.model.block.CubeModel;
import dev.ultreon.quantum.client.model.block.ModelProperties;
import dev.ultreon.quantum.client.model.entity.renderer.*;
import dev.ultreon.quantum.client.model.model.Json5ModelLoader;
import dev.ultreon.quantum.client.registry.BlockEntityModelRegistry;
import dev.ultreon.quantum.client.registry.BlockRenderPassRegistry;
import dev.ultreon.quantum.client.registry.EntityModelRegistry;
import dev.ultreon.quantum.client.registry.EntityRendererRegistry;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.world.FaceProperties;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Direction;

import java.util.Locale;

/**
 * Register rendering for entities, blocks, etc.
 *
 * @since 0.1.0
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class RenderingRegistration {
    /**
     * Register rendering for entities and blocks
     * @param client the QuantumClient instance
     */
    public static void registerRendering(QuantumClient client) {
        // Register block and entity models
        registerBlockModels();
        registerBlockEntityModels(client);

        // Register block renderers
        registerBlockRenderers();
        registerBlockRenderTypes();

        registerEntityRenderers();
    }

    /**
     * Registers block entity models.
     * @param client The QuantumClient instance.
     */
    private static void registerBlockEntityModels(QuantumClient client) {
        // Call the onRegister() method of the BLOCK_ENTITY_MODELS factory.
        ClientRegistrationEvents.BLOCK_ENTITY_MODELS.factory().onRegister();

        // Load block entity models using the client instance.
        BlockEntityModelRegistry.load(client);
    }

    /**
     * Registers block render types.
     */
    private static void registerBlockRenderTypes() {
        // Register the RenderType.WATER render type for the Blocks.WATER block.
        BlockRenderPassRegistry.register(Blocks.WATER, RenderPass.WATER);
        BlockRenderPassRegistry.register(Blocks.LEAVES, RenderPass.CUTOUT);

        // Call the onRegister() method of the BLOCK_RENDER_TYPES factory.
        ClientRegistrationEvents.BLOCK_RENDER_TYPES.factory().onRegister();
    }

    /**
     * Registers block renderers.
     */
    private static void registerBlockRenderers() {
        // Call the onRegister() method of the BLOCK_RENDERERS factory.
        ClientRegistrationEvents.BLOCK_RENDERERS.factory().onRegister();
    }

    /**
     * Registers block models for various blocks in the game.
     */
    private static void registerBlockModels() {
        // Register block models for grass block, log, and crafting bench
        BlockModelRegistry registry = BlockModelRegistry.get();
        registry.register(Blocks.GRASS_BLOCK, meta -> true, CubeModel.of(NamespaceID.of("blocks/grass"), NamespaceID.of("blocks/grass_top"), NamespaceID.of("blocks/dirt"), NamespaceID.of("blocks/grass_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        registry.register(Blocks.SNOWY_GRASS_BLOCK, meta -> true, CubeModel.of(NamespaceID.of("blocks/snowy_grass"), NamespaceID.of("blocks/snowy_grass_top"), NamespaceID.of("blocks/dirt"), NamespaceID.of("blocks/snowy_grass_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        registry.register(Blocks.SNOW_BLOCK, meta -> true, CubeModel.of(NamespaceID.of("blocks/snowy_grass_top"), NamespaceID.of("blocks/snowy_grass_top"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        registry.register(Blocks.LOG, meta -> true, CubeModel.of(NamespaceID.of("blocks/log"), NamespaceID.of("blocks/log"), NamespaceID.of("blocks/log"), NamespaceID.of("blocks/log_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        registry.register(Blocks.CRAFTING_BENCH, meta -> true, CubeModel.of(NamespaceID.of("blocks/crafting_bench"), NamespaceID.of("blocks/crafting_bench_top"), NamespaceID.of("blocks/crafting_bench_bottom"), NamespaceID.of("blocks/crafting_bench_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));

        // Register block models for blast furnace with different rotations based on metadata
        for (Direction direction : Direction.HORIZONTAL) {
            registry.register(Blocks.BLAST_FURNACE, meta -> meta.<Boolean>get("lit") && meta.<Direction>get("facing") == direction, CubeModel.of(NamespaceID.of("blocks/blast_furnace_" + direction.toString().toLowerCase(Locale.ROOT) + "_lit"), NamespaceID.of("blocks/blast_furnace_top"), NamespaceID.of("blocks/blast_furnace_bottom"), NamespaceID.of("blocks/blast_furnace_side"), NamespaceID.of("blocks/blast_furnace_front_lit"), ModelProperties.builder().rotateHorizontal(direction).build()));
            registry.register(Blocks.BLAST_FURNACE, meta -> !meta.<Boolean>get("lit") && meta.<Direction>get("facing") == direction, CubeModel.of(NamespaceID.of("blocks/blast_furnace_" + direction.toString().toLowerCase(Locale.ROOT)), NamespaceID.of("blocks/blast_furnace_top"), NamespaceID.of("blocks/blast_furnace_bottom"), NamespaceID.of("blocks/blast_furnace_side"), NamespaceID.of("blocks/blast_furnace_front"), ModelProperties.builder().rotateHorizontal(direction).build()));
        }

        registry.registerCustom(Blocks.PLANKS_SLAB, (meta) -> meta.<SlabBlock.Type>get("type") == SlabBlock.Type.TOP, () -> new Json5ModelLoader().load(Registries.BLOCK.getKey(Blocks.PLANKS_SLAB), NamespaceID.of("blocks/planks_slab_top")));
        registry.registerCustom(Blocks.PLANKS_SLAB, (meta) -> meta.<SlabBlock.Type>get("type") == SlabBlock.Type.BOTTOM, () -> new Json5ModelLoader().load(Registries.BLOCK.getKey(Blocks.PLANKS_SLAB), NamespaceID.of("blocks/planks_slab_bottom")));
        registry.registerCustom(Blocks.PLANKS_SLAB, (meta) -> meta.<SlabBlock.Type>get("type") == SlabBlock.Type.DOUBLE, () -> new Json5ModelLoader().load(Registries.BLOCK.getKey(Blocks.PLANKS_SLAB), NamespaceID.of("blocks/planks_slab_double")));

        // Trigger the block models factory registration event
        ClientRegistrationEvents.BLOCK_MODELS.factory().onRegister();

        // Register default block models for common blocks
        registry.registerDefault(Blocks.VOIDGUARD);
        registry.registerDefault(Blocks.ERROR);
        registry.registerDefault(Blocks.DIRT);
        registry.registerDefault(Blocks.SAND);
        registry.registerDefault(Blocks.SANDSTONE);
        registry.registerDefault(Blocks.GRAVEL);
        registry.registerDefault(Blocks.WATER);
        registry.registerDefault(Blocks.STONE);
        registry.registerDefault(Blocks.IRON_ORE);
        registry.registerDefault(Blocks.LEAVES);
        registry.registerDefault(Blocks.PLANKS);
        registry.registerDefault(Blocks.COBBLESTONE);
        registry.registerDefault(Blocks.TALL_GRASS);
    }

    /**
     * Registers entity models.
     */
    public static void registerEntityRendering(EntityModelRegistry entityModelManager) {
    }

    /**
     * Registers entity renderers.
     */
    public static void registerEntityRenderers() {
        // Register the entity models
        EntityModelRegistry entityModelManager = QuantumClient.get().entityModelManager;
        entityModelManager.registerBBModel(EntityTypes.PLAYER, NamespaceID.of("player"));
        entityModelManager.registerBBModel(EntityTypes.SOMETHING, NamespaceID.of("something"));
        entityModelManager.registerBBModel(EntityTypes.PIG, NamespaceID.of("pig"));
        entityModelManager.registerBBModel(EntityTypes.BANVIL, NamespaceID.of("banvil"));

        // Register the player entity renderer
        EntityRendererRegistry.register(EntityTypes.PLAYER, PlayerRenderer::new);

        // Register the dropped item entity renderer
        EntityRendererRegistry.register(EntityTypes.DROPPED_ITEM, DroppedItemRenderer::new);
        EntityRendererRegistry.register(EntityTypes.SOMETHING, SomethingRenderer::new);
        EntityRendererRegistry.register(EntityTypes.PIG, PigRenderer::new);
        EntityRendererRegistry.register(EntityTypes.BANVIL, BanvilRenderer::new);

        // Call the onRegister method of the factory in ENTITY_RENDERERS
        ClientRegistrationEvents.ENTITY_RENDERERS.factory().onRegister();
    }
}
