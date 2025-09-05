package dev.ultreon.quantum.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;

public class QuantumAgent {
    private static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        instrumentation = inst;
    }

    public static Class<?>[] getAllLoadedClasses() {
        if (instrumentation == null) {
            return new Class[0];
        }
        return instrumentation.getAllLoadedClasses();
    }
}
