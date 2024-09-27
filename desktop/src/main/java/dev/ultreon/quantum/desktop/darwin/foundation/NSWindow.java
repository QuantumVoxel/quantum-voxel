package dev.ultreon.quantum.desktop.darwin.foundation;

import com.sun.jna.Pointer;
import dev.ultreon.quantum.desktop.darwin.ObjC;
import dev.ultreon.quantum.desktop.darwin.ObjCObject;

public abstract class NSWindow extends ObjCObject {
    // NSWindow Style Mask Constants
    public static final long NSWindowStyleMaskBorderless = 0;
    public static final long NSWindowStyleMaskTitled = 1 << 0;
    public static final long NSWindowStyleMaskClosable = 1 << 1;
    public static final long NSWindowStyleMaskResizable = 1 << 3;

    public static NSWindow fromPtr(long windowID) {
        Pointer nsWindow = new Pointer(windowID);

        return new NSWindow() {
            @Override
            public Pointer getPointer() {
                return nsWindow;
            }
        };
    }

    public void makeFrameless() {
        setStyleMask(NSWindowStyleMaskBorderless | NSWindowStyleMaskResizable | NSWindowStyleMaskClosable);
    }

    public void setStyleMask(long styleMask) {
        Pointer setStyleMaskSelector = ObjC.INSTANCE.sel_getUid("setStyleMask:");

        ObjC.INSTANCE.objc_msgSend(getPointer(), setStyleMaskSelector, styleMask);
    }
}
