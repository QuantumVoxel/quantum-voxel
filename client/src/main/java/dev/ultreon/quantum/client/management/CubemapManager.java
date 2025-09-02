package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CubemapManager implements Manager<Cubemap>, Disposable {
    private final Map<NamespaceID, Cubemap> cubemaps = new HashMap<>();
    private final ResourceManager resourceManager;

    public CubemapManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public Cubemap register(@NotNull NamespaceID id, @NotNull Cubemap cubemap) {
        this.cubemaps.put(id, cubemap);
        return cubemap;
    }

    public void loadCubemap(NamespaceID id) {
        try (InputStream inputStream = resourceManager.openResourceStream(id)) {
            JsonValue root = CommonConstants.JSON_READ.parse(inputStream);

            String identifier = root.get("target_pos_x").asString();
            NamespaceID targetPosX = new NamespaceID(identifier);

            identifier = root.get("target_neg_x").asString();
            NamespaceID targetNegX = new NamespaceID(identifier);

            identifier = root.get("target_pos_y").asString();
            NamespaceID targetPosY = new NamespaceID(identifier);

            identifier = root.get("target_neg_y").asString();
            NamespaceID targetNegY = new NamespaceID(identifier);

            identifier = root.get("target_pos_z").asString();
            NamespaceID targetPosZ = new NamespaceID(identifier);

            identifier = root.get("target_neg_z").asString();
            NamespaceID targetNegZ = new NamespaceID(identifier);

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
    public @Nullable Cubemap get(NamespaceID id) {
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

    public int getLoadedCount() {
        return this.cubemaps.size();
    }
}
