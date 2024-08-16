package dev.ultreon.quantum.client.data;

import com.mojang.serialization.Codec;
import dev.ultreon.quantum.data.Json5Ops;
import dev.ultreon.quantum.util.NamespaceID;

public class ResourceWriter {
    private final ResourceOutput output;

    public ResourceWriter(ResourceOutput output) {
        this.output = output;
    }

    public <T> void write(NamespaceID resourceId, Codec<T> codec, T object) {
        codec.encodeStart(Json5Ops.INSTANCE, object).result().ifPresent(json5Element -> {
            output.write(resourceId, json5Element);
        });
    }
}
