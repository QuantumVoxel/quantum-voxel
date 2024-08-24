package dev.ultreon.quantum.world;

import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class DimensionInfo {
    public static final DimensionInfo OVERWORLD = new DimensionInfo(new NamespaceID("overworld"));
    private final NamespaceID id;

    public DimensionInfo(NamespaceID id) {
        this.id = id;
    }

    public NamespaceID getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DimensionInfo that = (DimensionInfo) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public TextObject getName() {
        return TextObject.translation(this.id.getDomain() + ".dimension." + this.id.getPath().replace('/', '.'));
    }
}
