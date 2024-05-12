package dev.ultreon.quantum.client.model.blockbench;

import java.util.Objects;

public final class BBMeta {
    private final String formatVersion;
    private final BBModelFormat modelFormat;
    private final boolean boxUv;

    public BBMeta(String formatVersion, BBModelFormat modelFormat, boolean boxUv) {
        this.formatVersion = formatVersion;
        this.modelFormat = modelFormat;
        this.boxUv = boxUv;
    }

    public String formatVersion() {
        return formatVersion;
    }

    public BBModelFormat modelFormat() {
        return modelFormat;
    }

    public boolean boxUv() {
        return boxUv;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBMeta) obj;
        return Objects.equals(this.formatVersion, that.formatVersion) &&
               Objects.equals(this.modelFormat, that.modelFormat) &&
               this.boxUv == that.boxUv;
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatVersion, modelFormat, boxUv);
    }

    @Override
    public String toString() {
        return "BBMeta[" +
               "formatVersion=" + formatVersion + ", " +
               "modelFormat=" + modelFormat + ", " +
               "boxUv=" + boxUv + ']';
    }

}
