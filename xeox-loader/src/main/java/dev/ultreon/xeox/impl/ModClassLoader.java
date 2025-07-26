package dev.ultreon.xeox.impl;

import dev.ultreon.logging.compat.ULoggerMarker;
import dev.ultreon.xeox.api.IFileSystem;
import dev.ultreon.xeox.api.IPath;
import dev.ultreon.xeox.impl.main.Main;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.util.*;

public class ModClassLoader extends ClassLoader implements IXeoxClassLoader {
    private final Map<String, Class<?>> loadedClasses = new HashMap<>();
    private final Set<String> failedClasses = new HashSet<>();
    private final IPermissionProvider provider;
    final Mod mod;
    private final GameClassLoader classLoader;
    final IFileSystem fs;
    private final List<IFileSystem> accessibleFs;
    private final Set<String> blocked = new HashSet<>();
    private final Set<String> mixinBlocked = new HashSet<>();
    private final XeoxLoader loader;

    public ModClassLoader(IPermissionProvider provider, Mod mod, GameClassLoader classLoader, IFileSystem fs, List<IFileSystem> accessibleFs, XeoxLoader loader) {
        this.provider = provider;
        this.mod = mod;
        this.classLoader = classLoader;
        this.fs = fs;
        this.accessibleFs = accessibleFs;
        this.loader = loader;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (failedClasses.contains(name)) {
            throw new ClassNotFoundException();
        }

        if (loadedClasses.containsKey(name)) {
            return loadedClasses.get(name);
        }

        if (name.startsWith("dev.ultreon.xeox.impl")) {
            throw new SecurityException("Mod '" + mod.modId() + "' tries to access class '" + name + "' which is prohibited as it's in the Xeox implementation package.");
        }

        if (name.startsWith("dev.ultreon.xeox.")) {
            return classLoader.loadClass(name);
        }

        if (blocked.contains(name)) {
            throw new SecurityException("Mod '" + mod.modId() + "' tries to access class '" + name + "' which is prohibited as it's blocked by the Xeox game provider.");
        }

        checkClass(Type.getObjectType(name.replace('.', '/')));

        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return classLoader.loadClass(name);
        }

        Class<?> aClass = classLoader.loadedClasses.get(name);
        if (aClass != null) {
            loadedClasses.put(name, aClass);
            return aClass;
        }

        IPath path = fs.path(name.replace('.', '/') + ".class");
        if (path.notExists()) {
            for (IFileSystem fs : classLoader.fileSystems) {
                path = fs.path(name.replace('.', '/') + ".class");
                if (path.exists()) {
                    try {
                        Class<?> aGameClass = classLoader.loadClass(name);
                        loadedClasses.put(name, aGameClass);
                        return aGameClass;
                    } catch (ClassNotFoundException e) {
                        failedClasses.add(name);
                        throw e;
                    }
                }
            }
        }


        try {
            byte[] bytes = path.readBytes();
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            Type superType = Type.getObjectType(node.superName);
            checkClass(superType);

            for (String type : node.interfaces) {
                checkClass(Type.getObjectType(type));
            }

            if (node.outerClass != null) {
                checkClass(Type.getObjectType(node.outerClass));
            }

            if (node.innerClasses != null) {
                for (InnerClassNode innerClassNode : node.innerClasses) {
                    checkClass(Type.getObjectType(innerClassNode.name));
                }
            }

            if (node.nestHostClass != null) {
                checkClass(Type.getObjectType(node.nestHostClass));
            }

            if (node.nestMembers != null) {
                for (String nestMember : node.nestMembers) {
                    checkClass(Type.getObjectType(nestMember));
                }
            }

            if (node.permittedSubclasses != null) {
                for (String permittedSubclass : node.permittedSubclasses) {
                    checkClass(Type.getObjectType(permittedSubclass));
                }
            }

            if (node.outerMethodDesc != null) {
                for (Type type : Type.getArgumentTypes(node.outerMethodDesc)) {
                    checkClass(type);
                }

                Type returnType = Type.getReturnType(node.outerMethodDesc);
                checkClass(returnType);
            }

            for (FieldNode field : node.fields) {
                Type fType = Type.getType(field.desc);
                checkClass(fType);

                if (field.value instanceof Type value) {
                    checkClass(value);
                }

                if (field.visibleAnnotations != null)
                    for (TypeAnnotationNode visibleTypeAnnotation : field.visibleTypeAnnotations) {
                        Type type = Type.getType(visibleTypeAnnotation.desc);
                        checkClass(type);

                        for (Object annotationValue : visibleTypeAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                    }

                if (field.invisibleAnnotations != null)
                    for (TypeAnnotationNode invisibleTypeAnnotation : field.invisibleTypeAnnotations) {
                        Type type = Type.getType(invisibleTypeAnnotation.desc);
                        checkClass(type);

                        for (Object annotationValue : invisibleTypeAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                    }

                if (field.visibleAnnotations != null)
                    for (AnnotationNode visibleAnnotation : field.visibleAnnotations) {
                        Type type = Type.getType(visibleAnnotation.desc);
                        checkClass(type);

                        for (Object annotationValue : visibleAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                    }

                if (field.invisibleAnnotations != null)
                    for (AnnotationNode invisibleAnnotation : field.invisibleAnnotations) {
                        Type type = Type.getType(invisibleAnnotation.desc);
                        checkClass(type);


                        for (Object annotationValue : invisibleAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                    }
            }

            if (node.recordComponents != null)
                for (RecordComponentNode recordComponent : node.recordComponents) {
                    Type fType = Type.getType(recordComponent.descriptor);
                    checkClass(fType);
                }

            if (node.visibleTypeAnnotations != null)
                for (TypeAnnotationNode visibleTypeAnnotation : node.visibleTypeAnnotations) {
                    Type type = Type.getType(visibleTypeAnnotation.desc);
                    checkClass(type);

                    List<Object> values = visibleTypeAnnotation.values;
                    detectMixin(type, values);

                    if (visibleTypeAnnotation.values != null)
                        for (Object annotationValue : visibleTypeAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                }

            if (node.invisibleTypeAnnotations != null)
                for (TypeAnnotationNode invisibleTypeAnnotation : node.invisibleTypeAnnotations) {
                    Type type = Type.getType(invisibleTypeAnnotation.desc);
                    checkClass(type);

                    List<Object> values = invisibleTypeAnnotation.values;
                    detectMixin(type, values);

                    if (invisibleTypeAnnotation.values != null)
                        for (Object annotationValue : invisibleTypeAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                }

            if (node.visibleAnnotations != null)
                for (AnnotationNode visibleAnnotation : node.visibleAnnotations) {
                    Type type = Type.getType(visibleAnnotation.desc);
                    checkClass(type);

                    List<Object> values = visibleAnnotation.values;
                    detectMixin(type, values);

                    if (visibleAnnotation.values != null)
                        for (Object annotationValue : visibleAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                }

            if (node.invisibleAnnotations != null)
                for (AnnotationNode invisibleAnnotation : node.invisibleAnnotations) {
                    Type type = Type.getType(invisibleAnnotation.desc);
                    checkClass(type);

                    List<Object> values = invisibleAnnotation.values;
                    detectMixin(type, values);

                    if (invisibleAnnotation.values != null)
                        for (Object annotationValue : invisibleAnnotation.values) {
                            if (annotationValue instanceof Type annotationValueType) {
                                checkClass(annotationValueType);
                            }
                        }
                }

            for (MethodNode method : node.methods) {
                Type returnType = Type.getReturnType(method.desc);
                checkClass(returnType);

                for (Type type : Type.getArgumentTypes(method.desc)) {
                    checkClass(type);
                }

                if (method.localVariables != null)
                    for (LocalVariableNode localVariableNode : method.localVariables) {
                        Type localType = Type.getType(localVariableNode.desc);
                        checkClass(localType);
                    }

                if (method.visibleLocalVariableAnnotations != null)
                    for (LocalVariableAnnotationNode visibleLocalVariableAnnotation : method.visibleLocalVariableAnnotations) {
                        Type localType = Type.getType(visibleLocalVariableAnnotation.desc);
                        checkClass(localType);

                        if (visibleLocalVariableAnnotation.values != null)
                            for (Object annotationValue : visibleLocalVariableAnnotation.values) {
                                if (annotationValue instanceof Type annotationValueType) {
                                    checkClass(annotationValueType);
                                }
                            }
                    }

                if (method.invisibleLocalVariableAnnotations != null)
                    for (LocalVariableAnnotationNode invisibleLocalVariableAnnotation : method.invisibleLocalVariableAnnotations) {
                        Type localType = Type.getType(invisibleLocalVariableAnnotation.desc);
                        checkClass(localType);


                        if (invisibleLocalVariableAnnotation.values != null)
                            for (Object annotationValue : invisibleLocalVariableAnnotation.values) {
                                if (annotationValue instanceof Type annotationValueType) {
                                    checkClass(annotationValueType);
                                }
                            }
                    }

                if (method.visibleTypeAnnotations != null)
                    for (TypeAnnotationNode visibleTypeAnnotation : method.visibleTypeAnnotations) {
                        Type type = Type.getType(visibleTypeAnnotation.desc);
                        checkClass(type);

                        if (visibleTypeAnnotation.values != null)
                            for (Object annotationValue : visibleTypeAnnotation.values) {
                                if (annotationValue instanceof Type annotationValueType) {
                                    checkClass(annotationValueType);
                                }
                            }
                    }

                if (method.invisibleTypeAnnotations != null)
                    for (TypeAnnotationNode invisibleTypeAnnotation : method.invisibleTypeAnnotations) {
                        Type type = Type.getType(invisibleTypeAnnotation.desc);
                        checkClass(type);

                        if (invisibleTypeAnnotation.values != null)
                            for (Object annotationValue : invisibleTypeAnnotation.values) {
                                if (annotationValue instanceof Type annotationValueType) {
                                    checkClass(annotationValueType);
                                }
                            }
                    }

                if (method.visibleAnnotations != null)
                    for (AnnotationNode visibleAnnotation : method.visibleAnnotations) {
                        Type type = Type.getType(visibleAnnotation.desc);
                        checkClass(type);

                        if (visibleAnnotation.values != null)
                            for (Object annotationValue : visibleAnnotation.values) {
                                if (annotationValue instanceof Type annotationValueType) {
                                    checkClass(annotationValueType);
                                }
                            }
                    }

                if (method.invisibleAnnotations != null)
                    for (AnnotationNode invisibleAnnotation : method.invisibleAnnotations) {
                        Type type = Type.getType(invisibleAnnotation.desc);
                        checkClass(type);

                        if (invisibleAnnotation.values != null)
                            for (Object annotationValue : invisibleAnnotation.values) {
                                if (annotationValue instanceof Type annotationValueType) {
                                    checkClass(annotationValueType);
                                }
                            }
                    }

                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof FieldInsnNode fieldInsnNode) {
                        Type owner = Type.getObjectType(fieldInsnNode.owner);
                        checkClass(owner);
                        Type desc = Type.getType(fieldInsnNode.desc);
                        checkClass(desc);
                    } else if (instruction instanceof MethodInsnNode methodInsnNode) {
                        Type owner = Type.getObjectType(methodInsnNode.owner);
                        checkClass(owner);

                        Type desc = Type.getMethodType(methodInsnNode.desc);
                        checkClass(desc);

                        checkMethod(owner, name, desc);
                    } else if (instruction instanceof TypeInsnNode typeInsnNode) {
                        Type desc = Type.getObjectType(typeInsnNode.desc);
                        checkClass(desc);
                    } else if (instruction instanceof InvokeDynamicInsnNode dynamicInsnNode) {
                        Type owner = Type.getObjectType(dynamicInsnNode.bsm.getOwner());
                        checkClass(owner);

                        Type bsmDesc = Type.getMethodType(dynamicInsnNode.bsm.getDesc());
                        checkClass(bsmDesc);

                        Type desc = Type.getMethodType(dynamicInsnNode.desc);
                        checkClass(desc);
                    } else if (instruction instanceof MultiANewArrayInsnNode multiANewArrayInsnNode) {
                        Type desc = Type.getObjectType(multiANewArrayInsnNode.desc);
                        checkClass(desc);
                    } else if (instruction instanceof LdcInsnNode ldcInsnNode) {
                        if (ldcInsnNode.cst instanceof Type type) {
                            provider.check(mod, "reflection:full");
                            checkClass(type);
                        }
                    } else if (instruction instanceof InvokeDynamicInsnNode dynamicInsnNode) {
                        Type desc = Type.getObjectType(dynamicInsnNode.desc);
                        checkClass(desc);
                    }
                }
            }

            Class<?> clazz = defineClass(name, bytes, 0, bytes.length);
            loadedClasses.put(name, clazz);
            return clazz;
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private void checkMethod(Type owner, String name, Type desc) {
        while (desc.getSort() == Type.ARRAY) {
            desc = desc.getElementType();
        }

        if (owner.getClassName().equals("java.lang.Class")) {
            // Check for anything that can get values or invoke stuff, annotations should be fine :3
            switch (name) {
                case "getDeclaredMethod", "invoke", "getEnumConstants", "getRecordComponents", "newInstance",
                     "getConstructors", "getDeclaredConstructors", "getFields", "getDeclaredFields", "getMethod" ->
                        provider.check(mod, "reflection:full");
            }
        } else if (owner.getClassName().equals("java.lang.System")) {
            switch (name) {
                case "exit" -> provider.check(mod, "system:exit");
                case "load", "loadLibrary" -> provider.check(mod, "natives:load");
            }
        } else if (owner.getClassName().equals("java.lang.Runtime")) {
            switch (name) {
                case "runFinalization" -> provider.check(mod, "runtime:finalization", "The mod '" + mod.modId() + "' tries to run the finalization method which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'runtime:finalization' and try again.");
                case "exec" -> provider.check(mod, "process:exec", "The mod '" + mod.modId() + "' tries to execute a command which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:exec' and try again.");
                case "addShutdownHook" -> provider.check(mod, "system:shutdown-hook", "The mod '" + mod.modId() + "' tries to add a shutdown hook which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'system:shutdown-hook' and try again.");
                case "exit" -> provider.check(mod, "system:exit", "The mod '" + mod.modId() + "' tries to exit the runtime which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'system:exit' and try again.");
                case "halt" -> provider.check(mod, "system:halt", "The mod '" + mod.modId() + "' tries to halt the runtime which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'system:exit' and try again.");
            }
        } else if (owner.getClassName().equals("java.lang.Thread")) {
            switch (name) {
                case "getContextClassLoader" -> provider.check(mod, "thread:getContextClassLoader", "The mod '" + mod.modId() + "' tries to get the context class loader which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'thread:getContextClassLoader' and try again.");
                case "setContextClassLoader" -> provider.check(mod, "thread:setContextClassLoader", "The mod '" + mod.modId() + "' tries to set the context class loader which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'thread:setContextClassLoader' and try again.");
                case "getStackTrace" -> provider.check(mod, "thread:getStackTrace", "The mod '" + mod.modId() + "' tries to get the stack trace which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'thread:getStackTrace' and try again.");
                case "dumpStack" -> provider.check(mod, "thread:dumpStack", "The mod '" + mod.modId() + "' tries to dump the stack which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'thread:dumpStack' and try again.");
                case "getAllStackTraces" -> provider.check(mod, "thread:getAllStackTraces", "The mod '" + mod.modId() + "' tries to get all stack traces which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'thread:getAllStackTraces' and try again.");
            }
        } else if (owner.getClassName().equals("java.lang.ProcessBuilder")) {
            if (name.equals("start")) {
                provider.check(mod, "process:start", "The mod '" + mod.modId() + "' tries to start a process which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:start' and try again.");
            }
        } else if (owner.getClassName().equals("java.lang.ProcessHandle")) {
            switch (name) {
                case "info" ->
                        provider.check(mod, "process:info", "The mod '" + mod.modId() + "' tries to get process information which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:info' and try again.");
                case "children" ->
                        provider.check(mod, "process:children", "The mod '" + mod.modId() + "' tries to get the children of a process which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:children' and try again.");
                case "descendants" ->
                        provider.check(mod, "process:descendants", "The mod '" + mod.modId() + "' tries to get the descendants of a process which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:descendants' and try again.");
                case "parent" ->
                        provider.check(mod, "process:parent", "The mod '" + mod.modId() + "' tries to get the parent of a process which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:parent' and try again.");
                case "onExit" ->
                        provider.check(mod, "process:onExit", "The mod '" + mod.modId() + "' tries to register a process exit handler which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:onExit' and try again.");
                case "destroy" ->
                        provider.check(mod, "process:destroy", "The mod '" + mod.modId() + "' tries to destroy a process which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:destroy' and try again.");
                case "supportsNormalTermination" ->
                        provider.check(mod, "process:normal-termination", "The mod '" + mod.modId() + "' tries to check if a process supports normal termination which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:normal-termination' and try again.");
                case "current" ->
                        provider.check(mod, "process:current", "The mod '" + mod.modId() + "' tries to get the current process which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'process:current' and try again.");
            }
        } else if (owner.getClassName().equals("java.lang.StackWalker")) {
            provider.check(mod, "reflection:full");
        }
    }

    private void detectMixin(Type type, List<Object> values) {
        if (values == null) return;

        if (type.getClassName().equals("org.spongepowered.asm.mixin.Mixin")) {
            for (int i = 0; i < values.size(); i += 2) {
                String valueName = (String) values.get(i);
                Object value = values.get(i + 1);
                if (valueName.equals("targets") && value instanceof String mixin) {
                    if (mixinBlocked.contains(mixin)) {
                        throw new SecurityException("Mod '" + mod.modId() + "' tries to mixin into '" + mixin + "' which is prohibited as it's blocked by the Xeox game provider.");
                    }
                } else if (valueName.equals("targets") && value instanceof String[] targets) {
                    for (String target : targets) {
                        if (mixinBlocked.contains(target)) {
                            throw new SecurityException("Mod '" + mod.modId() + "' tries to mixin inject into '" + target + "' which is prohibited as it's blocked by the Xeox game provider.");
                        }
                    }
                } else if (valueName.equals("value") && value instanceof String mixin) {
                    if (mixinBlocked.contains(mixin)) {
                        throw new SecurityException("Mod '" + mod.modId() + "' tries to mixin into '" + mixin + "' which is prohibited as it's blocked by the Xeox game provider.");
                    }
                } else if (valueName.equals("value") && value instanceof String[] targets) {
                    for (String target : targets) {
                        if (mixinBlocked.contains(target)) {
                        }
                    }
                } else if (valueName.equals("value") && value instanceof Type mixin) {
                    if (mixinBlocked.contains(mixin.getClassName())) {
                        throw new SecurityException("Mod '" + mod.modId() + "' tries to mixin into '" + mixin.getClassName() + "' which is prohibited as it's blocked by the Xeox game provider.");
                    }
                } else if (valueName.equals("value") && value instanceof Type[] mixins) {
                    for (Type mixin : mixins) {
                        if (mixinBlocked.contains(mixin.getClassName())) {
                            throw new SecurityException("Mod '" + mod.modId() + "' tries to mixin into '" + mixin.getClassName() + "' which is prohibited as it's blocked by the Xeox game provider.");
                        }
                    }
                } else if (valueName.equals("value")) {
                    throw new IllegalArgumentException("Invalid mixin value: " + value);
                } else if (valueName.equals("targets")) {
                    throw new IllegalArgumentException("Invalid mixin targets: " + value);
                }
            }
        }
    }

    private void checkClass(Type desc) {
        while (desc.getSort() == Type.ARRAY) {
            desc = desc.getElementType();
        }

        if (desc.getSort() == Type.METHOD) {
            for (Type type : desc.getArgumentTypes()) {
                checkClass(type);
            }
            checkClass(desc.getReturnType());
            return;
        }

        if (desc.getSort() != Type.OBJECT) {
            return;
        }
        if (desc.getClassName().startsWith("java.nio.") || desc.getClassName().startsWith("java.io.")) {
            provider.check(mod, "filesystem:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'filesystem:full' and try again.");
        }

        if (desc.getClassName().startsWith("java.net.")) {
            provider.check(mod, "network:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'network:full' and try again.");
        }

        if (desc.getClassName().startsWith("jdk.")
                || desc.getClassName().startsWith("java.util.jar.")
                || desc.getClassName().startsWith("java.util.zip.")
                || desc.getClassName().startsWith("java.util.prefs.")
                || desc.getClassName().startsWith("java.beans.")
                || desc.getClassName().startsWith("java.lang.instrument.")
                || desc.getClassName().startsWith("java.lang.management.")
                || desc.getClassName().startsWith("java.rmi.")
                || desc.getClassName().startsWith("org.xml.")
                || desc.getClassName().startsWith("org.w3c.dom.")
                || desc.getClassName().startsWith("sun.")
                || desc.getClassName().startsWith("com.sun.")
        ) {
            try {
                provider.check(mod, "jdk:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:full' and try again.");
            } catch (SecurityException e) {
                if (desc.getClassName().startsWith("java.util.jar.")) {
                    provider.check(mod, "jdk:java.util.jar", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:java.util.jar' and try again.");
                } else if (desc.getClassName().startsWith("java.util.zip.")) {
                    provider.check(mod, "jdk:java.util.zip", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:java.util.zip' and try again.");
                } else if (desc.getClassName().startsWith("java.util.prefs.")) {
                    provider.check(mod, "jdk:java.util.prefs", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:java.util.prefs' and try again.");
                } else if (desc.getClassName().startsWith("java.beans.")) {
                    provider.check(mod, "jdk:java.beans", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:java.beans' and try again.");
                } else if (desc.getClassName().startsWith("java.lang.instrument.")) {
                    provider.check(mod, "jdk:java.lang.instrument", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:java.lang.instrument' and try again.");
                } else if (desc.getClassName().startsWith("java.lang.management.")) {
                    provider.check(mod, "jdk:java.lang.management", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:java.lang.management' and try again.");
                } else if (desc.getClassName().startsWith("java.rmi.")) {
                    provider.check(mod, "jdk:java.rmi", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:java.rmi' and try again.");
                } else if (desc.getClassName().startsWith("org.xml.")) {
                    provider.check(mod, "jdk:org.xml", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:org.xml' and try again.");
                } else if (desc.getClassName().startsWith("org.w3c.dom.")) {
                    provider.check(mod, "jdk:org.w3c.dom", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:org.w3c.dom' and try again.");
                } else if (desc.getClassName().startsWith("sun.")) {
                    provider.check(mod, "jdk:sun", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:sun' and try again.");
                } else if (desc.getClassName().startsWith("com.sun.")) {
                    provider.check(mod, "jdk:com.sun", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'jdk:com.sun' and try again.");
                }
            }
        }
        if (desc.getClassName().startsWith("java.awt.")) {
            provider.check(mod, "awt:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'awt:full' and try again.");
        }

        if (desc.getClassName().startsWith("java.applet.")) {
            provider.check(mod, "applet:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'applet:full' and try again.");
        }

        if (desc.getClassName().startsWith("java.lang.reflect.") || desc.getClassName().equals("java.lang.ClassLoader")) {
            provider.check(mod, "reflection:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'reflection:full' and try again.");
        }

        if (desc.getClassName().startsWith("java.security.")) {
            provider.check(mod, "security:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'security:full' and try again.");
        }

        if (desc.getClassName().startsWith("java.sql.")) {
            provider.check(mod, "sql:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'sql:full' and try again.");
        }

        if (desc.getClassName().startsWith("javax.")) {
            provider.check(mod, "javax:full", "The mod '" + mod.modId() + "' tries to access the class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'javax:full' and try again.");
        }

        if (desc.getClassName().startsWith("java.util.")
                || desc.getClassName().startsWith("java.lang.")
                || desc.getClassName().startsWith("java.math.")
                || desc.getClassName().startsWith("java.time.")
                || desc.getClassName().startsWith("java.text.")
        ) {
            return;
        }

        for (IFileSystem fs : accessibleFs) {
            if (fs.path(desc.getInternalName() + ".class").exists()) {
                return;
            }
        }

        if (fs.path(desc.getInternalName() + ".class").exists()) {
            return;
        }

        try {
            provider.check(mod, "classpath:full");
        } catch (SecurityException e) {
            throw new SecurityException("Mod '" + mod.modId() + "' tries to access class '" + desc.getClassName() + "' which violates the applied permissions and is therefore not allowed. Please check your mod's permissions for 'classpath:full' and try again.");
        }
    }

    boolean requestPermission(String permission, Runnable runnable) {
        return provider.requestPermission(mod, permission, runnable);
    }

    public void block(String className) {
        blocked.add(className);
    }

    public void blockMixin() {
        mixinBlocked.add(mod.modId());
    }

    @Override
    public Enumeration<URL> getResources(String name) {
        IPath path = fs.path(name);
        List<URL> urls = new ArrayList<>();
        try {
            urls.add(new URL("xeox", mod.modId(), 69, path.path(), new XeoxURLStreamHandler(path)));
        } catch (IOException e) {
            Main.LOGGER.error("Failed to get resources for '{}'", name, e);
        }

        return Collections.enumeration(urls);
    }

    @Override
    public @Nullable URL getResource(String name) {
        IPath path = fs.path(name);
        try {
            return new URL("xeox", mod.modId(), 69, name.substring(1), new XeoxURLStreamHandler(path));
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public @Nullable InputStream getResourceAsStream(String name) {
        IPath path = fs.path(name);
        if (!path.exists()) {
            return null;
        }

        try {
            return path.read();
        } catch (IOException e) {
            return null;
        }
    }

    public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException {
        IPath path = fs.path(name.replace('.', '/') + ".class");
        if (!path.exists()) {
            throw new ClassNotFoundException(name);
        }

        try {
            ByteBuffer buffer;
            try (ByteChannel byteChannel = path.channel()) {
                long length = path.length();
                if (length > Integer.MAX_VALUE) {
                    throw new IOException("File is too large to fit in memory");
                }
                buffer = ByteBuffer.allocate((int) length);
                byteChannel.read(buffer);
                buffer.flip();
            }

            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            if (runTransformers) {
                for (IClassTransformer transformer : classLoader.getTransformers()) {
                    byte[] newBytes = transformer.transform(name, name, bytes);
                    if (newBytes != null) {
                        if (loader.isDevEnvironment() && !Arrays.equals(newBytes, bytes)) {
                            Main.LOGGER.info("Transformed class '{}' using transformer '{}'", name, transformer.getClass().getName());
                        }
                        bytes = newBytes;
                    }

                }
            }

            return bytes;
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private static class XeoxURLStreamHandler extends URLStreamHandler {
        private final IPath path;

        public XeoxURLStreamHandler(IPath path) {
            this.path = path;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            if (!path.exists()) {
                throw new FileNotFoundException(path.toString());
            }

            return new URLConnection(u) {
                @Override
                public void connect() throws IOException {
                    if (!path.exists()) {
                        throw new FileNotFoundException(path.toString());
                    }
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return path.read();
                }
            };
        }
    }
}
