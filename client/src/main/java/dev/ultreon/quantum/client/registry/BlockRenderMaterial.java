package dev.ultreon.quantum.client.registry;

import dev.ultreon.quantum.client.render.context.ColorSource;
import dev.ultreon.quantum.client.render.context.MaterialType;
import dev.ultreon.quantum.client.render.context.ObjectType;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import org.jetbrains.annotations.Nullable;

public class BlockRenderMaterial implements MaterialType {

    public static final RenderMaterial OPAQUE = new RenderMaterial(new BlockRenderMaterial(BlockRenderType.SOLID), ObjectType.BLOCK, "Solid Block");
    public static final RenderMaterial TRANSPARENT = new RenderMaterial(new BlockRenderMaterial(BlockRenderType.TRANSPARENT), ObjectType.BLOCK, "Transparent Block");
    public static final RenderMaterial CUTOUT = new RenderMaterial(new BlockRenderMaterial(BlockRenderType.CUTOUT), ObjectType.BLOCK, "Cutout Block");
    public static final RenderMaterial WATER = new RenderMaterial(new BlockRenderMaterial(BlockRenderType.WATER), ObjectType.WATER, "Water");
    public static final RenderMaterial LEAVES = new RenderMaterial(new BlockRenderMaterial(BlockRenderType.CUTOUT), ObjectType.BLOCK, "Leaves");

    private final BlockRenderType blockRenderType;

    public BlockRenderMaterial(BlockRenderType blockRenderType) {
        this.blockRenderType = blockRenderType;
    }

    @Override
    public @Nullable ColorSource getDiffuse() {
        return null;
    }

    @Override
    public @Nullable ColorSource getSpecular() {
        return null;
    }

    @Override
    public @Nullable ColorSource getEmission() {
        return null;
    }

    @Override
    public @Nullable ColorSource getReflectiveness() {
        return null;
    }

    @Override
    public @Nullable ColorSource getAmbient() {
        return null;
    }

    @Override
    public @Nullable ColorSource getShininess() {
        return null;
    }

    @Override
    public @Nullable ColorSource getTransparency() {
        return null;
    }

    @Override
    public boolean doesMerging() {
        return blockRenderType == BlockRenderType.SOLID || blockRenderType == BlockRenderType.TRANSPARENT || blockRenderType == BlockRenderType.CUTOUT || blockRenderType == BlockRenderType.WATER;
    }

    public BlockRenderType getBlockRenderType() {
        return blockRenderType;
    }
}
