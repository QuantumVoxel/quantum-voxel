package dev.ultreon.quantum.desktop.darwin;

import ca.weblite.objc.RuntimeUtils;
import com.sun.jna.Pointer;

import java.lang.reflect.Constructor;

public abstract class ObjCObject {
    public abstract Pointer getPointer();

    protected final Pointer __msgSend(String selector, Object... args) {
        Pointer selector1 = RuntimeUtils.sel(selector);
        if (selector1.equals(Pointer.NULL)) throw new RuntimeException("Failed to find selector: " + selector);
        return ObjC.INSTANCE.objc_msgSend(getPointer(), selector1, args);
    }

    protected final String __msgSendString(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getString(0);
    }

    protected final Pointer __msgSendPointer(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer;
    }

    protected final byte __msgSendByte(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getByte(0);
    }

    protected final short __msgSendShort(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getShort(0);
    }

    protected final int __msgSendInt(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getInt(0);
    }

    protected final long __msgSendLong(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getLong(0);
    }

    protected final float __msgSendFloat(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getFloat(0);
    }

    protected final double __msgSendDouble(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getDouble(0);
    }

    protected final boolean __msgSendBoolean(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return pointer.getByte(0) != 0;
    }

    protected final ObjCClass __msgSendClass(String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);
        return ObjCClass.getClass(pointer);
    }

    protected final ObjCObject __msgSendObject(Class<? extends ObjCObject> returnType, String selector, Object... args) {
        Pointer pointer = __msgSend(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + selector);

        try {
            Constructor<? extends ObjCObject> objCObject = returnType.getConstructor(Pointer.class);
            objCObject.setAccessible(true);
            return objCObject.newInstance(pointer);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Objective-C return type: " + returnType, e);
        }
    }

    protected static Pointer __msgSend(ObjCClass receiver, String selector, Object... args) {
        return receiver.msgPointer(selector, args);
    }

    protected static ObjCObject __msgSendObject(Class<? extends ObjCObject> returnType, ObjCClass receiver, String selector, Object... args) {
        Pointer pointer = receiver.msgPointer(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + receiver.__getClassName() + ":" + selector);

        try {
            Constructor<? extends ObjCObject> objCObject = returnType.getConstructor(Pointer.class);
            objCObject.setAccessible(true);
            return objCObject.newInstance(pointer);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Objective-C return type: " + returnType, e);
        }
    }

    protected static byte __msgSendByte(ObjCClass receiver, String selector, Object... args) {
        Pointer pointer = receiver.msgPointer(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + receiver.__getClassName() + ":" + selector);
        return pointer.getByte(0);
    }

    protected static short __msgSendShort(ObjCClass receiver, String selector, Object... args) {
        Pointer pointer = receiver.msgPointer(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + receiver.__getClassName() + ":" + selector);
        return pointer.getShort(0);
    }

    protected static int __msgSendInt(ObjCClass receiver, String selector, Object... args) {
        Pointer pointer = receiver.msgPointer(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + receiver.__getClassName() + ":" + selector);
        return pointer.getInt(0);
    }

    protected static long __msgSendLong(ObjCClass receiver, String selector, Object... args) {
        Pointer pointer = receiver.msgPointer(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + receiver.__getClassName() + ":" + selector);
        return pointer.getLong(0);
    }

    protected static String __msgSendString(ObjCClass receiver, String selector, Object... args) {
        Pointer pointer = receiver.msgPointer(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + receiver.__getClassName() + ":" + selector);
        return pointer.getString(0);
    }

    protected static Pointer __msgSendPointer(ObjCClass receiver, String selector, Object... args) {
        Pointer pointer = receiver.msgPointer(selector, args);
        if (pointer.equals(Pointer.NULL)) throw new RuntimeException("Failed to send message: " + receiver.__getClassName() + ":" + selector);
        return pointer;
    }

    public final String __clsName() {
        return ObjCClass.getClassName(getPointer());
    }

    public final ObjCClass __cls() {
        return ObjCClass.getClass(__clsName());
    }
}
