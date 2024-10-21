package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.desktop.darwin.foundation.NSApplication;
import dev.ultreon.quantum.desktop.darwin.foundation.NSImage;

import java.io.IOException;

public class MacOSDockIcon {
    public static void setDockIcon(FileHandle imagePath) throws IOException {
        NSImage icon = new NSImage(imagePath);

        NSApplication application = NSApplication.sharedApplication();
        application.setApplicationIconImage(icon);
    }

}