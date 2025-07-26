package dev.ultreon.quantum.desktop;

public class XeoxDesktopLauncher {
    public static void main(String[] args) {
        try {
            DesktopLauncher.main(args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(StatusCode.forException());
        }
    }
}
