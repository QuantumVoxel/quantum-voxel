package dev.ultreon.quantum.client.render.world;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.registry.BlockRenderMaterial;
import dev.ultreon.quantum.client.registry.BlockRenderType;
import dev.ultreon.quantum.client.render.context.ObjectType;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.render.context.TextureSrc;
import dev.ultreon.quantum.client.render.material.EntityMaterial;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class RenderMaterials {
    public static final RenderMaterial ENTITY = new RenderMaterial(new EntityMaterial(), ObjectType.ENTITY, "Entity");
    public static @Nullable RenderMaterial BLOCK_OVERlAY = new RenderMaterial(new BlockRenderMaterial(BlockRenderType.OVERLAY), ObjectType.BLOCK_OVERLAY, "Block Overlay");

    public static RenderMaterial sun(NamespaceID texture) {
        Supplier<Texture> textureSupplier = () -> QuantumClient.get().getTextureManager().getTexture(texture);
        TextureSrc sun1 = new TextureSrc(textureSupplier, "Sun", false);
        SunMaterial sun = new SunMaterial(sun1);
        return new RenderMaterial(sun, null, "Sun");
    }

    public static RenderMaterial moon(NamespaceID texture) {
        Supplier<Texture> textureSupplier = () -> QuantumClient.get().getTextureManager().getTexture(texture);
        TextureSrc sun1 = new TextureSrc(textureSupplier, "Moon", false);
        MoonMaterial moon = new MoonMaterial(sun1);
        return new RenderMaterial(moon, null, "Moon");
    }
}
