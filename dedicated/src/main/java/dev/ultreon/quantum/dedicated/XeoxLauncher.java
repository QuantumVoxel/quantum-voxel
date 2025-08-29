package dev.ultreon.quantum.dedicated;

import dev.ultreon.quantum.CommonConstants;

import java.io.IOException;

public class XeoxLauncher {
    public static void main(String[] args) {
        try {
            Main.main(args);
        } catch (IOException | InterruptedException e) {
            CommonConstants.LOGGER.error("Failed to start server: " + e.getMessage());
            Runtime.getRuntime().halt(1);
        }
    }
}
