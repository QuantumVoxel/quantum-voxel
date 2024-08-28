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

public record BBTexture(String path, String name, String folder, String namespace, String id, int width, int height,
                        int uvWidth, int uvHeight, boolean particle, boolean layersEnabled, String syncToProject,
                        String renderMode, String renderSides, int frameTime, String frameOrderType, String frameOrder,
                        boolean frameInterpolate, boolean visible, boolean internal, boolean saved, UUID uuid,
                        String relativePath, URI data) {

    public Texture loadOrGetTexture() throws IOException {
        URI data1 = data;
        URL url;
        if (data1.getScheme().equals("data")) {
            url = new URL(null, data1.toString(), new DataURL());
        } else {
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
