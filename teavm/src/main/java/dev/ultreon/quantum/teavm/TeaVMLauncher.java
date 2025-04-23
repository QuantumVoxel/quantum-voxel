package dev.ultreon.quantum.teavm;

import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashLog;
import org.teavm.jso.JSExceptions;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSError;

/**
 * Launches the TeaVM/HTML application.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        if (Window.current().getLocation().getFullURL().matches("https?://[0-9]+.discordsays.com(/.*)?")
            && !Window.current().getLocation().getFullURL().matches("https?://[0-9]+.discordsays.com/.proxy(/.*)?")) {
            Console.warn("Discord says detected, redirecting to proxy");
            Window.current().getLocation().setPathName("/.proxy/");
            return;
        }

        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");

        config.width = 0;
        config.height = 0;
        config.usePhysicalPixels = true;
        config.alpha = true;
        config.stencil = true;
        config.premultipliedAlpha = true;
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
