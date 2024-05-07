package dev.ultreon.quantum.premain;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.google.common.collect.Lists;
import dev.ultreon.gameprovider.quantum.QuantumVxlGameProvider;
import dev.ultreon.quantum.premain.posix.CLibrary;
import dev.ultreon.quantum.premain.win32.Kernel32;
import net.fabricmc.loader.impl.launch.knot.KnotClient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Pre-main class for quantum.
 * <p style="color: red;">NOTE: Internal API!</p>
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@ApiStatus.Internal
public final class PreMain {
    /**
     * Production main method.
     * <p style="color: red;">NOTE: Internal API!</p>
     *
     * @param args Arguments to pass to the game.
     */
    @ApiStatus.Internal
    public static void main(String[] args) {
        List<String> argv = Lists.newArrayList(args);
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        if (argv.remove("--packaged")) {
            PreMain.setDirectory();
        }
        KnotClient.main(args);
    }

    private static void setDirectory() {
        @NotNull Path launchPath = QuantumVxlGameProvider.getDataDir();
        if (SharedLibraryLoader.isWindows) {
            Kernel32.INSTANCE.SetCurrentDirectoryW(launchPath.toString().toCharArray());
        } else if (SharedLibraryLoader.isLinux || SharedLibraryLoader.isMac) {
            CLibrary.INSTANCE.chdir(launchPath.toString());
        }
    }
}
