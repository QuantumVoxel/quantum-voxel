package dev.ultreon.quantum.desktop;

import com.sun.tools.attach.*;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

public class DesktopLauncher {
    private static Instrumentation instrumentation;

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("Agent attached!");
        instrumentation = inst;
    }

    public static Class<?>[] getLoadedClasses() {
        if (instrumentation != null) {
            return instrumentation.getAllLoadedClasses();
        }
        try {
            Class<?> aClass = Class.forName("dev.ultreon.quantum.agent.QuantumAgent");
            Field field = aClass.getDeclaredField("instrumentation");
            field.setAccessible(true);
            instrumentation = (Instrumentation) field.get(null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new Class[0];
        }

        if (instrumentation == null) {
            return new Class[0];
        }
        return instrumentation.getAllLoadedClasses();
    }

    /**
     * Launches the game.
     * <p style="color:red;"><b>Note: This method should not be called.</b></p>
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    public static void main(String[] argv) {
        DesktopMain.launch(argv);
    }
}
