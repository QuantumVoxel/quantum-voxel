package dev.ultreon.quantum.desktop.nativebridge;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class NativeBridge {
    public static MethodNode mapMethod(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            int access = getAccess(method);
            List<String> list = new ArrayList<>();
            for (Class<?> aClass : method.getExceptionTypes()) {
                String internalName = Type.getInternalName(aClass);
                list.add(internalName);
            }
            return new MethodNode(
                    access,
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    null,
                    list.toArray(new String[0])
            );
        }

        return null;
    }

    private static int getAccess(Method method) {
        int access = Modifier.isPublic(method.getModifiers())
                ? Modifier.PUBLIC
                : Modifier.isProtected(method.getModifiers())
                ? Modifier.PROTECTED
                : Modifier.isPrivate(method.getModifiers())
                ? Modifier.PRIVATE
                : 0;
        access |= Modifier.STATIC;
        if (Modifier.isFinal(method.getModifiers())) {
            access |= Modifier.FINAL;
        }
        access |= Modifier.NATIVE;
        return access;
    }

    public static native void loadLibrary(String filepath);
}
