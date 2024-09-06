package dev.ultreon.quantum.client.model.blockbench;

import java.util.Objects;
import java.util.UUID;

public record BBModelElementReference(UUID uuid) implements BBModelOutlineInfo {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBModelElementReference) obj;
        return Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public String toString() {
        return "BBModelElementReference[" +
               "uuid=" + uuid + ']';
    }

}
