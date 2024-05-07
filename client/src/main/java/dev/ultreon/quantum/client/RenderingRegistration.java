package dev.ultreon.quantum.client;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.SlabBlock;
import dev.ultreon.quantum.client.api.events.ClientRegistrationEvents;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.model.block.CubeModel;
import dev.ultreon.quantum.client.model.block.ModelProperties;
import dev.ultreon.quantum.client.model.entity.renderer.DroppedItemRenderer;
import dev.ultreon.quantum.client.model.entity.renderer.PigRenderer;
import dev.ultreon.quantum.client.model.entity.renderer.PlayerRenderer;
import dev.ultreon.quantum.client.model.entity.renderer.SomethingRenderer;
import dev.ultreon.quantum.client.model.model.Json5ModelLoader;
import dev.ultreon.quantum.client.registry.BlockEntityModelRegistry;
import dev.ultreon.quantum.client.registry.BlockRenderTypeRegistry;
import dev.ultreon.quantum.client.registry.EntityModelRegistry;
import dev.ultreon.quantum.client.registry.EntityRendererRegistry;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.world.FaceProperties;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.world.CubicDirection;

/**
 * Register rendering for entities, blocks, etc.
 *
 * @since 0.1.0
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
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
        BlockRenderTypeRegistry.register(Blocks.WATER, RenderLayer.WATER);

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
        BlockModelRegistry.register(Blocks.GRASS_BLOCK, meta -> true, CubeModel.of(QuantumClient.id("blocks/grass_top"), QuantumClient.id("blocks/dirt"), QuantumClient.id("blocks/grass_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.LOG, meta -> true , CubeModel.of(QuantumClient.id("blocks/log"), QuantumClient.id("blocks/log"), QuantumClient.id("blocks/log_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.CRAFTING_BENCH, meta -> true, CubeModel.of(QuantumClient.id("blocks/crafting_bench_top"), QuantumClient.id("blocks/crafting_bench_bottom"), QuantumClient.id("blocks/crafting_bench_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));

        // Register block models for switch test block based on metadata
        BlockModelRegistry.register(Blocks.META_SWITCH_TEST, meta -> meta.<Boolean>getProperty("on").value, CubeModel.of(QuantumClient.id("blocks/switch_on")));
        BlockModelRegistry.register(Blocks.META_SWITCH_TEST, meta -> !meta.<Boolean>getProperty("on").value, CubeModel.of(QuantumClient.id("blocks/switch_off")));

        // Register block models for blast furnace with different rotations based on metadata
        for (CubicDirection direction : CubicDirection.HORIZONTAL) {
            BlockModelRegistry.register(Blocks.BLAST_FURNACE, meta -> meta.<Boolean>getProperty("lit").value && meta.<CubicDirection>getProperty("facing").value == direction, CubeModel.of(QuantumClient.id("blocks/blast_furnace_top"), QuantumClient.id("blocks/blast_furnace_bottom"), QuantumClient.id("blocks/blast_furnace_side"), QuantumClient.id("blocks/blast_furnace_front_lit"), ModelProperties.builder().rotateHorizontal(direction).build()));
            BlockModelRegistry.register(Blocks.BLAST_FURNACE, meta -> !meta.<Boolean>getProperty("lit").value && meta.<CubicDirection>getProperty("facing").value == direction, CubeModel.of(QuantumClient.id("blocks/blast_furnace_top"), QuantumClient.id("blocks/blast_furnace_bottom"), QuantumClient.id("blocks/blast_furnace_side"), QuantumClient.id("blocks/blast_furnace_front"), ModelProperties.builder().rotateHorizontal(direction).build()));
        }

        BlockModelRegistry.registerCustom(Blocks.PLANKS_SLAB, (meta) -> meta.<SlabBlock.Type>getProperty("type").value == SlabBlock.Type.TOP, () -> new Json5ModelLoader().load(Registries.BLOCK.getKey(Blocks.PLANKS_SLAB), QuantumClient.id("blocks/planks_slab_top")));
        BlockModelRegistry.registerCustom(Blocks.PLANKS_SLAB, (meta) -> meta.<SlabBlock.Type>getProperty("type").value == SlabBlock.Type.BOTTOM, () -> new Json5ModelLoader().load(Registries.BLOCK.getKey(Blocks.PLANKS_SLAB), QuantumClient.id("blocks/planks_slab_bottom")));
        BlockModelRegistry.registerCustom(Blocks.PLANKS_SLAB, (meta) -> meta.<SlabBlock.Type>getProperty("type").value == SlabBlock.Type.DOUBLE, () -> new Json5ModelLoader().load(Registries.BLOCK.getKey(Blocks.PLANKS_SLAB), QuantumClient.id("blocks/planks_slab_double")));

        // Trigger the block models factory registration event
        ClientRegistrationEvents.BLOCK_MODELS.factory().onRegister();

        // Register default block models for common blocks
        BlockModelRegistry.registerDefault(Blocks.VOIDGUARD);
        BlockModelRegistry.registerDefault(Blocks.ERROR);
        BlockModelRegistry.registerDefault(Blocks.DIRT);
        BlockModelRegistry.registerDefault(Blocks.SAND);
        BlockModelRegistry.registerDefault(Blocks.SANDSTONE);
        BlockModelRegistry.registerDefault(Blocks.GRAVEL);
        BlockModelRegistry.registerDefault(Blocks.WATER);
        BlockModelRegistry.registerDefault(Blocks.STONE);
        BlockModelRegistry.registerDefault(Blocks.IRON_ORE);
        BlockModelRegistry.registerDefault(Blocks.LEAVES);
        BlockModelRegistry.registerDefault(Blocks.PLANKS);
        BlockModelRegistry.registerDefault(Blocks.COBBLESTONE);
        BlockModelRegistry.registerDefault(Blocks.TALL_GRASS);
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
        EntityModelRegistry entityModelManager = QuantumClient.get().entityModelManager;
        entityModelManager.registerBBModel(EntityTypes.PLAYER, QuantumClient.id("player"));
        entityModelManager.registerBBModel(EntityTypes.SOMETHING, QuantumClient.id("something"));
        entityModelManager.registerBBModel(EntityTypes.PIG, QuantumClient.id("pig"));

        // Register the player entity renderer
        EntityRendererRegistry.register(EntityTypes.PLAYER, PlayerRenderer::new);

        // Register the dropped item entity renderer
        EntityRendererRegistry.register(EntityTypes.DROPPED_ITEM, DroppedItemRenderer::new);
        EntityRendererRegistry.register(EntityTypes.SOMETHING, SomethingRenderer::new);
        EntityRendererRegistry.register(EntityTypes.PIG, PigRenderer::new);

        // Call the onRegister method of the factory in ENTITY_RENDERERS
        ClientRegistrationEvents.ENTITY_RENDERERS.factory().onRegister();
    }
}
