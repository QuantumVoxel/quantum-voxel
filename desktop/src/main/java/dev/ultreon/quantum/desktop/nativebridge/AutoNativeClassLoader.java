package dev.ultreon.quantum.desktop.nativebridge;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class AutoNativeClassLoader extends ClassLoader {
    private static final Map<Class<?>, Class<?>> nativeClasses = Collections.synchronizedMap(new WeakHashMap<>());

    public AutoNativeClassLoader() {
        super("auto-native", AutoNativeClassLoader.class.getClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.endsWith("$$UltreonJV$NativeImpl$")) {
            String actualName = name.substring(0, name.length() - 15);
            Class<?> aClass = getParent().loadClass(actualName);
            if (resolve) {
                resolveClass(aClass);
            }

            Class<?> aClass1 = nativeClasses.computeIfAbsent(aClass, this::mapClass);
            if (aClass1 == null) {
                throw new ClassNotFoundException(name);
            }
            return aClass1;
        }

        return super.loadClass(name, resolve);
    }

    private Class<?> mapClass(Class<?> c) {
        if (Modifier.isFinal(c.getModifiers())) return null;

        ClassNode node = new ClassNode();
        node.version = Opcodes.V21;
        node.access = Opcodes.ACC_PUBLIC;
        node.name = Type.getInternalName(c) + "$$UltreonJV$NativeImpl$";
        node.superName = Type.getInternalName(c);
        for (Method method : c.getMethods()) {
            if (method.isSynthetic()) continue;
            if (method.isBridge()) continue;
            if (!Modifier.isPublic(method.getDeclaringClass().getModifiers()) && !Modifier.isProtected(method.getDeclaringClass().getModifiers())) continue;

            node.methods.add(NativeBridge.mapMethod(method));
        }

        MethodNode constructor = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE, "$$createNative0", "([Ljava/lang/Object)V", null, null);
        node.methods.add(constructor);
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "this$$nativeHandle", null, null, -1L));

        boolean hasConstructor = false;
        if (!c.isInterface()) {
            for (Constructor<?> constructor1 : c.getDeclaredConstructors()) {
                hasConstructor = true;

                Type type = Type.getType(constructor1);
                Type[] argumentTypes = new Type[type.getArgumentTypes().length + 1];
                System.arraycopy(type.getArgumentTypes(), 0, argumentTypes, 1, type.getArgumentTypes().length);
                argumentTypes[0] = Type.LONG_TYPE;
                Type newType = Type.getMethodType(type.getReturnType(), argumentTypes);
                MethodNode constructor2 = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", newType.getDescriptor(), null, null);
                node.methods.add(constructor2);
                for (int i = 0; i < argumentTypes.length - 1; i++) {
                    constructor2.visitVarInsn(argumentTypes[i].getOpcode(Opcodes.ILOAD), i + 2);
                }

                constructor2.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(c), "<init>", type.getDescriptor(), false));
                constructor2.instructions.add(new VarInsnNode(Opcodes.LLOAD, 1));
                constructor2.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, node.name, "this$$nativeHandle", Type.getInternalName(c)));
                constructor2.instructions.add(new InsnNode(Opcodes.RETURN));
                node.methods.add(constructor2);
            }

            if (!hasConstructor) {
                MethodNode constructor2 = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE), null, null);
                node.methods.add(constructor2);
                constructor2.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                try {
                    constructor2.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(c.getConstructor().getDeclaringClass()), "<init>", "()V", false));
                } catch (NoSuchMethodException e) {
                    throw new LinkageError("Failed to link to parent constructor", e);
                }
                constructor2.instructions.add(new VarInsnNode(Opcodes.LLOAD, 1));
                constructor2.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, node.name, "this$$nativeHandle", Type.getInternalName(c)));
                constructor2.instructions.add(new InsnNode(Opcodes.RETURN));
                node.methods.add(constructor2);
            }
        } else {
            MethodNode constructor2 = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.LONG_TYPE), null, null);
            node.methods.add(constructor2);
            constructor2.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            try {
                constructor2.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(c.getConstructor().getDeclaringClass()), "<init>", "()V", false));
            } catch (NoSuchMethodException e) {
                throw new LinkageError("Failed to link to parent constructor", e);
            }
            constructor2.instructions.add(new VarInsnNode(Opcodes.LLOAD, 1));
            constructor2.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, node.name, "this$$nativeHandle", Type.getInternalName(c)));
            constructor2.instructions.add(new InsnNode(Opcodes.RETURN));
            node.methods.add(constructor2);
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);

        return defineClass(c.getName() + "--NativeBridge$", writer.toByteArray(), 0, writer.toByteArray().length);
    }
}
