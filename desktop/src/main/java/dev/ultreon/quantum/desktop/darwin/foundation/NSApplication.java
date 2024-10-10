package dev.ultreon.quantum.desktop.darwin.foundation;

import com.sun.jna.Pointer;
import dev.ultreon.quantum.desktop.darwin.ObjCClass;
import dev.ultreon.quantum.desktop.darwin.ObjCObject;

public abstract class NSApplication extends ObjCObject {
    public static NSApplication sharedApplication() {
        // Call NSApplication::sharedApplication
        ObjCClass nsAppClass = ObjCClass.getClass("NSApplication");
        Pointer nsApp = nsAppClass.msgPointer("sharedApplication");

        return new NSApplication() {
            @Override
            public Pointer getPointer() {
                return nsApp;
            }
        };
    }

    public void setApplicationIconImage(NSImage icon) {
        __msgSend("setApplicationIconImage:", icon.getPointer());
    }

    public NSImage getApplicationIconImage() {
        return new NSImage(__msgSend("applicationIconImage"));
    }
}
