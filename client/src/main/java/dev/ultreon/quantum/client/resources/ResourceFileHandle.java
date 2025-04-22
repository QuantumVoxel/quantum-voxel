package dev.ultreon.quantum.client.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * A file handle for resources.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ResourceFileHandle extends FileHandle {
    private final NamespaceID id;
    private final @Nullable Resource resource;

    /**
     * Constructs a new ResourceFileHandle with the given namespace ID.
     * 
     * @param id The namespace ID of the resource.
     */
    public ResourceFileHandle(NamespaceID id) {
        super(id.toString());
        this.id = id;
        this.resource = QuantumClient.get().getResourceManager().getResource(id);
    }

    /**
     * Constructs a new ResourceFileHandle with the given resource.
     * 
     * @param resource The resource.
     */
    public ResourceFileHandle(@NotNull Resource resource) {
        super("generated_" + UUID.randomUUID().toString().replace("-", ""));

        this.id = new NamespaceID("java", "generated_" + UUID.randomUUID().toString().replace("-", ""));
        this.resource = resource;
    }

    /**
     * Gets the namespace ID of the resource.
     * 
     * @return The namespace ID of the resource.
     */
    public NamespaceID getId() {
        return this.id;
    }

    /**
     * Gets the resource.
     * 
     * @return The resource.
     */
    public @Nullable Resource getResource() {
        return this.resource;
    }

    /**
     * Reads the resource.
     * 
     * @return The resource.
     */
    @Override
    public InputStream read() {
        if (this.resource == null) throw new GdxRuntimeException(String.format("Resource %s not found", this.id));
        try {
            return this.resource.openStream();
        } catch (IOException e) {
            throw new GdxRuntimeException(String.format("An IO error occurred while reading resource %s", this.id), e);
        }
    }

    /**
     * Checks if the resource exists.
     * 
     * @return True if the resource exists, false otherwise.
     */
    @Override
    public boolean exists() {
        return this.resource != null;
    }
}
