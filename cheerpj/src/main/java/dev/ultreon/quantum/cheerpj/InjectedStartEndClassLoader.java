package dev.ultreon.quantum.cheerpj;

import dev.ultreon.gameprovider.quantum.AnsiColors;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class InjectedStartEndClassLoader extends ClassLoader {
    private final Map<String, Class<?>> classes = new HashMap<>();

    public InjectedStartEndClassLoader() {
        super(InjectedStartEndClassLoader.class.getClassLoader());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!name.startsWith("java.") && !name.startsWith("javax.")) {
            System.out.println(AnsiColors.PURPLE + "[DEBUG]: " + AnsiColors.RESET + "Loading class " + name);
            Class<?> aClass = classes.get(name);
            if (aClass != null) {
                System.out.println(AnsiColors.PURPLE + "[DEBUG]: " + AnsiColors.RESET + "Found cached class " + name);
                return aClass;
            }
            Class<?> aClass1 = findClass(name);
            if (resolve) {
                System.out.println(AnsiColors.PURPLE + "[DEBUG]: " + AnsiColors.RESET + "Resolved class " + name);
                resolveClass(aClass1);
            }
            return aClass1;
        }

        Class<?> aClass = getParent().loadClass(name);
        if (resolve) {
            resolveClass(aClass);
        }
        return aClass;
    }

    protected Class<?> findClass(final String name)
            throws ClassNotFoundException {
        try (InputStream is = getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (is == null) {
                System.err.println(AnsiColors.RED + "[ERROR]" + AnsiColors.RESET + "Could not find class " + name);
                throw new ClassNotFoundException(name);
            }
            byte[] b = is.readAllBytes();
            b = transform(b);
            Class<?> aClass = defineClass(name, b, 0, b.length, new ProtectionDomain(new CodeSource(Path.of("/app/lib/fabric-loader-0.16.14.jar").toUri().toURL(), (Certificate[]) null), null));
            this.classes.put(name, aClass);
            return aClass;
        } catch (IOException e) {
            System.out.println(AnsiColors.RED + "[ERROR]" + AnsiColors.RESET + "Failed to load class " + name + ": " + e.getMessage());
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] transform(byte[] b) {
        ClassReader cr = new ClassReader(b);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        // Replace contents of com.badlogic.gdx.utils.BufferUtils contents of with emu.com.badlogic.gdx.utils.BufferUtils
        if (cn.name.equals("com/badlogic/gdx/utils/BufferUtils")) {
            try (InputStream stream = getParent().getResourceAsStream("emu/com/badlogic/gdx/utils/BufferUtilsEmu.class")) {
                if (stream != null) {
                    ClassReader crEmu = new ClassReader(stream);
                    ClassNode cnEmu = new ClassNode();
                    crEmu.accept(cnEmu, 0);

                    cn.fields = cnEmu.fields;
                    cn.methods = cnEmu.methods;
                } else {
                    throw new RuntimeException("Could not find emulated BufferUtils class!");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Inject "System.out.println("<ANSI-PURPLE>[DEBUG] Method called: <method-name>");" where <ANSI-PURPLE> is the actual ANSI PURPLE code.
        for (MethodNode mn : cn.methods) {
            // Skip native and abstract methods.
            if ((mn.access & (ACC_NATIVE | ACC_ABSTRACT)) != 0) {
                continue;
            }
            if (mn.name.equals("<clinit>")) {
                InsnList list = new InsnList();
                list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                list.add(new LdcInsnNode(AnsiColors.PURPLE + "[DEBUG]:" + AnsiColors.RESET + " Class initializer called: " + Type.getObjectType(cn.name).getClassName() + "\n"));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V"));
                mn.instructions.insert(list);
            }

            for (AbstractInsnNode node : mn.instructions.toArray()) {
                mn.instructions.insertBefore(node, new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                mn.instructions.insertBefore(node, new LdcInsnNode(AnsiColors.PURPLE + "[DEBUG]:" + AnsiColors.RESET + " Opcode called: " + node.getClass().getName() + " @ " + node.getOpcode() + "\n"));
                mn.instructions.insertBefore(node, new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V"));
            }

            if (mn.name.equals("<clinit>") || mn.name.equals("<init>")) {
                continue;
            }

            InsnList list = new InsnList();
            list.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
            list.add(new LdcInsnNode(AnsiColors.PURPLE + "[DEBUG]:" + AnsiColors.RESET + " Method called: " + Type.getObjectType(cn.name).getClassName() + "." + mn.name + "\n"));
            list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(Ljava/lang/String;)V"));
            mn.instructions.insert(list);
        }

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }
}
