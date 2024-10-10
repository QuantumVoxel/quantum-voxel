package dev.ultreon.quantum.desktop.darwin;

import ca.weblite.objc.RuntimeUtils;
import com.sun.jna.Pointer;

public final class ObjCClass {
    private final Pointer pointer;
    private String className = null;

    private ObjCClass(Pointer pointer) {
        this.className = RuntimeUtils.clsName(pointer);
        this.pointer = pointer;
    }

    private ObjCClass(String className, Pointer pointer) {
        this.className = className;
        this.pointer = pointer;
    }

    public static String getClassName(Pointer pointer) {
        return ObjCClass.getClass(pointer).className;
    }

    public Pointer getPointer() {
        return pointer;
    }

    public static ObjCClass getClass(String name) {
        Pointer ptr = RuntimeUtils.cls(name);

        if (ptr == null || ptr.equals(Pointer.NULL))
            throw new RuntimeException("Failed to get class " + name);

        return new ObjCClass(name, ptr);
    }

    public static ObjCClass getClass(Pointer ptr) {
        if (ptr == null || ptr.equals(Pointer.NULL))
            throw new RuntimeException("Failed to get class");

        return new ObjCClass(ptr);
    }

    public Pointer msgPointer(String selector, Object... args) {
        Pointer pointer1 = RuntimeUtils.msgPointer(pointer, selector, args);
        if (pointer1 == null)
            throw new RuntimeException("Failed to send message: '" + selector + "'");
        return pointer1;
    }

    public Pointer msgPointer(Pointer selector, Object... args) {
        Pointer pointer1 = RuntimeUtils.msgPointer(pointer, selector, args);
        if (pointer1.equals(Pointer.NULL))
            throw new RuntimeException("Failed to send message: '" + RuntimeUtils.selName(selector) + "'");
        return pointer1;
    }

    public byte msgByte(String selector, Object... args) {
        byte b = RuntimeUtils.msgPointer(pointer, selector, args).getByte(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public byte msgByte(Pointer selector, Object... args) {
        byte b = ObjC.INSTANCE.objc_msgSend(pointer, selector, args).getByte(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public short msgShort(String selector, Object... args) {
        short b = RuntimeUtils.msgPointer(pointer, selector, args).getShort(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public short msgShort(Pointer selector, Object... args) {
        short b = ObjC.INSTANCE.objc_msgSend(pointer, selector, args).getShort(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public int msgInt(String selector, Object... args) {
        int b = RuntimeUtils.msgPointer(pointer, selector, args).getInt(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public int msgInt(Pointer selector, Object... args) {
        int b = ObjC.INSTANCE.objc_msgSend(pointer, selector, args).getInt(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public long msgLong(String selector, Object... args) {
        long b = RuntimeUtils.msgPointer(pointer, selector, args).getLong(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public long msgLong(Pointer selector, Object... args) {
        long b = ObjC.INSTANCE.objc_msgSend(pointer, selector, args).getLong(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public float msgFloat(String selector, Object... args) {
        float b = RuntimeUtils.msgPointer(pointer, selector, args).getFloat(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public float msgFloat(Pointer selector, Object... args) {
        float b = ObjC.INSTANCE.objc_msgSend(pointer, selector, args).getFloat(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public double msgDouble(String selector, Object... args) {
        double b = RuntimeUtils.msgPointer(pointer, selector, args).getDouble(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public double msgDouble(Pointer selector, Object... args) {
        double b = ObjC.INSTANCE.objc_msgSend(pointer, selector, args).getDouble(0);
        if (b == 0) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public String msgString(String selector, Object... args) {
        String b = RuntimeUtils.msgPointer(pointer, selector, args).getString(0);
        if (b == null) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public String msgString(Pointer selector, Object... args) {
        String b = ObjC.INSTANCE.objc_msgSend(pointer, selector, args).getString(0);
        if (b == null) throw new RuntimeException("Failed to send message: '" + selector + "'");
        return b;
    }

    public String __getClassName() {
        return className;
    }
}
