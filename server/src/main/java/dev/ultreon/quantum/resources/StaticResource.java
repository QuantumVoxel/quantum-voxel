package dev.ultreon.quantum.resources;

import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.libs.commons.v0.util.IOUtils;
import dev.ultreon.libs.functions.v0.misc.ThrowingSupplier;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StaticResource implements Resource, Closeable {
    private final NamespaceID id;
    protected ThrowingSupplier<InputStream, IOException> opener;
    private byte[] data;

    public StaticResource(NamespaceID id, ThrowingSupplier<InputStream, IOException> opener) {
        this.id = id;
        this.opener = opener;
    }

    @Override
    public void load() {
        try (InputStream inputStream = this.opener.get()) {
            this.data = IOUtils.readAllBytes(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLoaded() {
        return this.data != null;
    }

    public InputStream loadOrOpenStream() {
        byte[] buf = this.loadOrGet();
        return buf == null ? null : new ByteArrayInputStream(buf);
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    @Deprecated
    public ByteArrayInputStream openStream() {
        byte[] buf = this.loadOrGet();
        return buf == null ? null : new ByteArrayInputStream(buf);
    }

    public NamespaceID id() {
        return this.id;
    }

    public @Nullable JsonValue readJson() {
        byte[] bytes = this.loadOrGet();
        if (bytes == null) return null;

        return CommonConstants.JSON_READ.parse(new String(bytes, StandardCharsets.UTF_8));
    }

    public void close() {
        this.data = null;
    }
}
