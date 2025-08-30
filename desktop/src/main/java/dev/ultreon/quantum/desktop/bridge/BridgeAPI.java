package dev.ultreon.quantum.desktop.bridge;

import java.util.Map;

public interface BridgeAPI {
    boolean fireEvent(String event, Object... args);

    void main(Map<String, String> mapping);
}
