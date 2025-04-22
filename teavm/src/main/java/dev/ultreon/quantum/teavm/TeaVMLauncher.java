package dev.ultreon.quantum.teavm;

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import dev.ultreon.quantum.client.Main;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import org.jetbrains.annotations.NotNull;
import org.teavm.jso.JSExceptions;
import org.teavm.jso.core.JSError;

/**
 * Launches the TeaVM/HTML application.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");

        //// If width and height are each greater than 0, then the app will use a fixed size.
        //config.width = 640;
        //config.height = 480;
        //// If width and height are both 0, then the app will use all available space.
        config.width = 0;
        config.height = 0;
        //// If width and height are both -1, then the app will fill the canvas size.
//        config.width = -1;
//        config.height = -1;
        config.usePhysicalPixels = true;
        config.alpha = true;
        config.stencil = true;
        config.premultipliedAlpha = true;
        config.useGL30 = true;
        config.shouldEncodePreference = true;

        SafeLoadWrapper safeWrapper = new SafeLoadWrapper(args);
        try {
            JSError.catchNative(() -> {
                new TeaVMPlatform(safeWrapper);
                new TeaApplication(safeWrapper, config);
                return null;
            }, e -> {
                Throwable javaException = JSExceptions.getJavaException(e);
                safeWrapper.crash(javaException);
                return null;
            });
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            String string = crashLog.toString();
            Console.error(string);
        } catch (Throwable e) {
            safeWrapper.crash(e);
            Console.error("Error: " + e.getMessage() + " (" + e.getClass().getName() + ")");
        }
    }
}
