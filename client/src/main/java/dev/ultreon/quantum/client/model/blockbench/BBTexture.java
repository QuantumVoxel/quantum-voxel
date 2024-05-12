package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.Texture;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ByteArrayFileHandle;
import dev.ultreon.quantum.client.uri.DataURL;
import dev.ultreon.quantum.client.uri.DataUrlHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public final class BBTexture {
    private final String path;
    private final String name;
    private final String folder;
    private final String namespace;
    private final String id;
    private final int width;
    private final int height;
    private final int uvWidth;
    private final int uvHeight;
    private final boolean particle;
    private final boolean layersEnabled;
    private final String syncToProject;
    private final String renderMode;
    private final String renderSides;
    private final int frameTime;
    private final String frameOrderType;
    private final String frameOrder;
    private final boolean frameInterpolate;
    private final boolean visible;
    private final boolean internal;
    private final boolean saved;
    private final UUID uuid;
    private final String relativePath;
    private final URI data;

    public BBTexture(String path, String name, String folder, String namespace, String id, int width, int height,
                     int uvWidth, int uvHeight, boolean particle, boolean layersEnabled, String syncToProject,
                     String renderMode, String renderSides, int frameTime, String frameOrderType, String frameOrder,
                     boolean frameInterpolate, boolean visible, boolean internal, boolean saved, UUID uuid,
                     String relativePath, URI data) {
        this.path = path;
        this.name = name;
        this.folder = folder;
        this.namespace = namespace;
        this.id = id;
        this.width = width;
        this.height = height;
        this.uvWidth = uvWidth;
        this.uvHeight = uvHeight;
        this.particle = particle;
        this.layersEnabled = layersEnabled;
        this.syncToProject = syncToProject;
        this.renderMode = renderMode;
        this.renderSides = renderSides;
        this.frameTime = frameTime;
        this.frameOrderType = frameOrderType;
        this.frameOrder = frameOrder;
        this.frameInterpolate = frameInterpolate;
        this.visible = visible;
        this.internal = internal;
        this.saved = saved;
        this.uuid = uuid;
        this.relativePath = relativePath;
        this.data = data;
    }

    public Texture loadOrGetTexture() throws IOException {
        URI data1 = data;
        URL url;
        if (data1.getScheme().equals("data")) {
            url = new URL(null, data1.toString(), new DataURL());
        } else  {
            url = data1.toURL();
        }
        try (InputStream inputStream = url.openStream()) {
            return QuantumClient.invokeAndWait(() -> {
                Texture texture1 = new Texture(new ByteArrayFileHandle(".png", inputStream.readAllBytes()));
                texture1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                return texture1;
            });
        }
    }

    public String path() {
        return path;
    }

    public String name() {
        return name;
    }

    public String folder() {
        return folder;
    }

    public String namespace() {
        return namespace;
    }

    public String id() {
        return id;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int uvWidth() {
        return uvWidth;
    }

    public int uvHeight() {
        return uvHeight;
    }

    public boolean particle() {
        return particle;
    }

    public boolean layersEnabled() {
        return layersEnabled;
    }

    public String syncToProject() {
        return syncToProject;
    }

    public String renderMode() {
        return renderMode;
    }

    public String renderSides() {
        return renderSides;
    }

    public int frameTime() {
        return frameTime;
    }

    public String frameOrderType() {
        return frameOrderType;
    }

    public String frameOrder() {
        return frameOrder;
    }

    public boolean frameInterpolate() {
        return frameInterpolate;
    }

    public boolean visible() {
        return visible;
    }

    public boolean internal() {
        return internal;
    }

    public boolean saved() {
        return saved;
    }

    public UUID uuid() {
        return uuid;
    }

    public String relativePath() {
        return relativePath;
    }

    public URI data() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBTexture) obj;
        return Objects.equals(this.path, that.path) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.folder, that.folder) &&
               Objects.equals(this.namespace, that.namespace) &&
               Objects.equals(this.id, that.id) &&
               this.width == that.width &&
               this.height == that.height &&
               this.uvWidth == that.uvWidth &&
               this.uvHeight == that.uvHeight &&
               this.particle == that.particle &&
               this.layersEnabled == that.layersEnabled &&
               Objects.equals(this.syncToProject, that.syncToProject) &&
               Objects.equals(this.renderMode, that.renderMode) &&
               Objects.equals(this.renderSides, that.renderSides) &&
               this.frameTime == that.frameTime &&
               Objects.equals(this.frameOrderType, that.frameOrderType) &&
               Objects.equals(this.frameOrder, that.frameOrder) &&
               this.frameInterpolate == that.frameInterpolate &&
               this.visible == that.visible &&
               this.internal == that.internal &&
               this.saved == that.saved &&
               Objects.equals(this.uuid, that.uuid) &&
               Objects.equals(this.relativePath, that.relativePath) &&
               Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name, folder, namespace, id, width, height, uvWidth, uvHeight, particle, layersEnabled, syncToProject, renderMode, renderSides, frameTime, frameOrderType, frameOrder, frameInterpolate, visible, internal, saved, uuid, relativePath, data);
    }

    @Override
    public String toString() {
        return "BBTexture[" +
               "path=" + path + ", " +
               "name=" + name + ", " +
               "folder=" + folder + ", " +
               "namespace=" + namespace + ", " +
               "id=" + id + ", " +
               "width=" + width + ", " +
               "height=" + height + ", " +
               "uvWidth=" + uvWidth + ", " +
               "uvHeight=" + uvHeight + ", " +
               "particle=" + particle + ", " +
               "layersEnabled=" + layersEnabled + ", " +
               "syncToProject=" + syncToProject + ", " +
               "renderMode=" + renderMode + ", " +
               "renderSides=" + renderSides + ", " +
               "frameTime=" + frameTime + ", " +
               "frameOrderType=" + frameOrderType + ", " +
               "frameOrder=" + frameOrder + ", " +
               "frameInterpolate=" + frameInterpolate + ", " +
               "visible=" + visible + ", " +
               "internal=" + internal + ", " +
               "saved=" + saved + ", " +
               "uuid=" + uuid + ", " +
               "relativePath=" + relativePath + ", " +
               "data=" + data + ']';
    }

}
