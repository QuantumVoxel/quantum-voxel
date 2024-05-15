package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.utils.Disposable;
import de.marhali.json5.Json5Object;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CubemapManager implements Manager<Cubemap>, Disposable {
    private final Map<Identifier, Cubemap> cubemaps = new LinkedHashMap<>();
    private final ResourceManager resourceManager;

    public CubemapManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public Cubemap register(@NotNull Identifier id, @NotNull Cubemap cubemap) {
        this.cubemaps.put(id, cubemap);
        return cubemap;
    }

    public void loadCubemap(Identifier id) {
        try (InputStream inputStream = resourceManager.openResourceStream(id)) {
            Json5Object root = CommonConstants.JSON5.parse(inputStream).getAsJson5Object();

            String identifier = root.getAsJson5Primitive("target_pos_x").getAsString();
            Identifier targetPosX = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_neg_x").getAsString();
            Identifier targetNegX = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_pos_y").getAsString();
            Identifier targetPosY = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_neg_y").getAsString();
            Identifier targetNegY = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_pos_z").getAsString();
            Identifier targetPosZ = new Identifier(identifier);

            identifier = root.getAsJson5Primitive("target_neg_z").getAsString();
            Identifier targetNegZ = new Identifier(identifier);

            Cubemap cubemap = new Cubemap(
                    new ResourceFileHandle(targetPosX.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetNegX.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetPosY.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetNegY.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetPosZ.mapPath(p -> "textures/cubemap/" + p + ".png")),
                    new ResourceFileHandle(targetNegZ.mapPath(p -> "textures/cubemap/" + p + ".png"))
            );

            this.register(id, cubemap);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load cubemap {}", id, e);
        }
    }

    @Override
    public @Nullable Cubemap get(Identifier id) {
        if (!this.cubemaps.containsKey(id)) {
            this.loadCubemap(id);
        }

        return this.cubemaps.get(id);
    }

    public void reload(ReloadContext context) {
        for (Cubemap cubemap : List.copyOf(this.cubemaps.values())) {
            context.submit(cubemap::dispose);
        }

        this.cubemaps.clear();
    }

    @Override
    public void dispose() {
        for (Cubemap cubemap : this.cubemaps.values()) {
            cubemap.dispose();
        }

        this.cubemaps.clear();
    }
}
