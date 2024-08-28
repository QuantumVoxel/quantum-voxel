package dev.ultreon.quantum.client.model.blockbench;

import java.util.Objects;

public record BBMeta(String formatVersion, BBModelFormat modelFormat, boolean boxUv) {

    @Override
    public String toString() {
        return "BBMeta[" +
               "formatVersion=" + formatVersion + ", " +
               "modelFormat=" + modelFormat + ", " +
               "boxUv=" + boxUv + ']';
    }

}
