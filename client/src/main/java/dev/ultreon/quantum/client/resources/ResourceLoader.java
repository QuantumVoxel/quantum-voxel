package dev.ultreon.quantum.client.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.model.ModelType;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.util.NamespaceID;

import static org.jetbrains.annotations.ApiStatus.Experimental;
import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A utility class for loading resources.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ResourceLoader {
    private static final G3dModelLoader g3djLoader = new G3dModelLoader(new JsonReader());
    private static final G3dModelLoader g3dbLoader = new G3dModelLoader(new UBJsonReader());

    /**
     * Loads a model from a static resource.
     *
     * @param resource the static resource to load a model from.
     * @return the loaded model.
     */
    public static Model loadG3D(NamespaceID resource) {
        FileHandle file = QuantumClient.resource(resource.mapPath(path -> "models/" + path));

        TextureProvider textureProvider = fileName -> {
            String s = "assets/" + resource.getDomain() + "/models";
            if (fileName.startsWith(s)) {
                String filePath = fileName.substring(s.length() + 1);
                return new Texture(QuantumClient.resource(resource.mapPath(path -> "textures/" + filePath)));
            }
            return new Texture(QuantumClient.resource(resource.withPath("textures/" + fileName)).path());
        };

        if (resource.getPath().endsWith(".g3dj")) return g3djLoader.loadModel(file, textureProvider);
        else if (resource.getPath().endsWith(".g3db")) return g3dbLoader.loadModel(file, textureProvider);
        throw new GdxRuntimeException("Unsupported G3D model type: " + file.extension());
    }

    /**
     * Loads a model from a dynamic resource.
     * <p><b>WARNING: THIS METHOD IS EXPERIMENTAL, AND CAN LEAD TO UNEXPECTED BEHAVIOR.</b></p>
     *
     * @param resource the dynamic resource to load a model from.
     * @param type     the model type.
     * @return the loaded model.
     */
    @Experimental
    public static Model loadG3D(Resource resource, ModelType type, TextureProvider textureProvider) {
        if (type == ModelType.G3DJ) return g3djLoader.loadModel(new ResourceFileHandle(resource), textureProvider);
        if (type == ModelType.G3DB) return g3dbLoader.loadModel(new ResourceFileHandle(resource), textureProvider);
        throw new GdxRuntimeException("Unsupported G3D model type: " + type.name());
    }

    /**
     * Initializes the resource loader utility class.
     * @param client the client instance.
     */
    @Internal
    public static void init(QuantumClient client) {

    }
}
