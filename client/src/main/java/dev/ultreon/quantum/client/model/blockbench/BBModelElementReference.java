package dev.ultreon.quantum.client.model.blockbench;

import java.util.Objects;
import java.util.UUID;

public final class BBModelElementReference implements BBModelOutlineInfo {
    private final UUID uuid;

    public BBModelElementReference(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBModelElementReference) obj;
        return Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "BBModelElementReference[" +
               "uuid=" + uuid + ']';
    }

}
