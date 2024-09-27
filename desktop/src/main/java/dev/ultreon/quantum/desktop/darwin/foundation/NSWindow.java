package dev.ultreon.quantum.desktop.darwin.foundation;

import com.sun.jna.Pointer;
import dev.ultreon.quantum.desktop.darwin.ObjCObject;

public abstract class NSWindow extends ObjCObject {
    // NSWindow Style Mask Constants
    public static final long NSWindowStyleMaskBorderless = 0;
    public static final long NSWindowStyleMaskTitled = 1 << 0;
    public static final long NSWindowStyleMaskClosable = 1 << 1;
    public static final long NSWindowStyleMaskResizable = 1 << 3;
    public static final long NSWindowStyleMaskMiniaturizable = 1 << 4;
    public static final long NSWindowStyleMaskFullScreen = 1 << 5;
    public static final long NSWindowStyleMaskFullSizeContentView = 1 << 6;
    public static final long NSWindowStyleMaskUtilityWindow = 1 << 7;
    public static final long NSWindowStyleMaskDocModalWindow = 1 << 8;
    public static final long NSWindowStyleMaskNonactivatingPanel = 1 << 9;
    public static final long NSWindowStyleMaskTakesTitleFromPreviousWindow = 1 << 10;
    public static final long NSWindowStyleMaskUnifiedTitleAndToolbar = 1 << 12;
    public static final long NSWindowStyleMaskFullScreenAuxiliary = 1 << 13;
    public static final long NSWindowStyleMaskHidesToolbarButton = 1 << 14;
    public static final long NSWindowStyleMaskHidesTitleBar = 1 << 15;
    public static final long NSWindowStyleMaskHidesTitleBarWhenVerticallyAttached = 1 << 16;
    public static final long NSWindowStyleMaskHidesTitleBarWhenHorizontallyAttached = 1 << 17;
    public static final long NSWindowStyleMaskCantBecomeKeyWindow = 1 << 18;
    public static final long NSWindowStyleMaskCantBecomeMainWindow = 1 << 19;
    public static final long NSWindowStyleMaskIsVisible = 1 << 20;
    public static final long NSWindowStyleMaskCanHide = 1 << 21;



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
        long defaultStyleMask = NSWindowStyleMaskTitled
                                | NSWindowStyleMaskClosable
                                | NSWindowStyleMaskMiniaturizable
                                | NSWindowStyleMaskResizable;
        setStyleMask(defaultStyleMask | NSWindowStyleMaskFullScreen | NSWindowStyleMaskHidesTitleBar);
    }

    public void setStyleMask(long styleMask) {
        __msgSend("setStyleMask:", styleMask);
    }
}
