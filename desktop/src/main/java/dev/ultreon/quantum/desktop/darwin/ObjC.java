package dev.ultreon.quantum.desktop.darwin;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface ObjC extends Library {
    ObjC INSTANCE = Native.load("objc", ObjC.class);

    Pointer objc_getClass(String className);

    Pointer sel_getUid(String selectorName);

    Pointer objc_msgSend(Pointer receiver, Pointer selector, Object... args);
}
