package dev.ultreon.quantum.desktop;

import org.slf4j.LoggerFactory;

public class XeoxDesktopLauncher {
    public static void main(String[] args) {
        try {
            DesktopLauncher.main(args);
        } catch (Throwable t) {
            LoggerFactory.getLogger(XeoxDesktopLauncher.class).error("Failed to launch game", t);
            System.exit(StatusCode.forException());
        }
    }
}
