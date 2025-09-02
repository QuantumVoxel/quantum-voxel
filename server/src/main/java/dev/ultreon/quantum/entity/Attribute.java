package dev.ultreon.quantum.entity;

import java.util.Objects;

public final class Attribute {
    public static final Attribute SPEED = new Attribute("quantum.generic.speed");
    public static final Attribute BLOCK_REACH = new Attribute("quantum.generic.block_reach");
    public static final Attribute ENTITY_REACH = new Attribute("quantum.generic.entity_reach");
    private final String key;

    public Attribute(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Attribute) obj;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "Attribute[" +
               "key=" + key + ']';
    }

}
