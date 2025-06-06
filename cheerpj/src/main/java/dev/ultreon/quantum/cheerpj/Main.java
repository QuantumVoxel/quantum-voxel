package dev.ultreon.quantum.cheerpj;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import dev.ultreon.gameprovider.quantum.AnsiColors;
import org.lwjgl.Sys;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        InjectedStartEndClassLoader cl = new InjectedStartEndClassLoader();
        Class<?> mainClass = cl.loadClass("dev.ultreon.quantum.lwjgl2.DesktopLauncher");
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    private static void debug(String s) {
        System.out.print(AnsiColors.PURPLE + "[DEBUG]: " + AnsiColors.RESET + s + "\n");
    }

    private static void uncaughtException(Thread t, Throwable e) {
        System.err.print(AnsiColors.RED + "[ERROR]" + AnsiColors.RESET + "Uncaught exception in thread " + t.getName() + "\n");
        for (StackTraceElement element : e.getStackTrace()) {
            System.err.print(AnsiColors.RED + "[ERROR]" + AnsiColors.RESET + "    at " + element.toString() + AnsiColors.RESET + "\n");
        }
    }
}