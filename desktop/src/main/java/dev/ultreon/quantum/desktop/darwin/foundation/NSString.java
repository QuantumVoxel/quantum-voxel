package dev.ultreon.quantum.desktop.darwin.foundation;

import com.sun.jna.Pointer;
import dev.ultreon.quantum.desktop.darwin.ObjCClass;
import dev.ultreon.quantum.desktop.darwin.ObjCObject;

public class NSString extends ObjCObject {
    private final Pointer pointer;

    public NSString(String string) {
        if (string == null) {
            throw new NullPointerException("string is null");
        }

        ObjCClass nsStringClass = ObjCClass.getClass("NSString");

        this.pointer = nsStringClass.msgPointer("stringWithUTF8String:", string);
        if (pointer.equals(Pointer.NULL))
            throw new RuntimeException("Failed to allocate an NSString instance.");
    }

    public NSString(Pointer pointer) {
        this.pointer = pointer;
    }

    @Override
    public Pointer getPointer() {
        return pointer;
    }
}
