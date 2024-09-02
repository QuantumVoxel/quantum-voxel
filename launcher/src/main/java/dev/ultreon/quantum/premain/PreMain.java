package dev.ultreon.quantum.premain;

import com.google.common.collect.Lists;
import dev.ultreon.gameprovider.quantum.QuantumVxlGameProvider;
import dev.ultreon.quantum.desktop.StartupHelper;
import net.fabricmc.loader.impl.launch.knot.KnotClient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        if (argv.remove("--debug")) System.setProperty("fabric.log.level", "debug");
        if (argv.remove("--server")) System.setProperty("fabric.side", "server");
        else System.setProperty("fabric.side", "client");

        System.setProperty("log4j2.formatMsgNoLookups", "true");
//        System.setProperty("fabric.development", "true");
        System.setProperty("fabric.log.disableAnsi", "true");

        // Copy mixinprovider.jar to ./mods/
        try {
            Files.copy(PreMain.class.getResourceAsStream("/mixinprovider.jar"), Paths.get("mods/mixinprovider.jar"));
        } catch (IOException e) {
            e.printStackTrace();
            Runtime.getRuntime().exit(1);
        }

        args = argv.toArray(new String[0]);
        KnotClient.main(args);
    }

    private static void setDirectory() {
        @NotNull Path launchPath = QuantumVxlGameProvider.getDataDir();
        if (!launchPath.toAbsolutePath().equals(Path.of(".").toAbsolutePath())) {
            try {
                if (Files.notExists(launchPath))
                    Files.createDirectories(launchPath);
            } catch (IOException e) {
                e.printStackTrace();
                Runtime.getRuntime().exit(4);
            }

            System.out.println("Setting directory to " + launchPath);
            StartupHelper.startNewJvmIfRequired(true, launchPath);
            System.out.println("Exiting");
            System.exit(0);
        } else {
            System.out.println("Skipping directory setup");
        }
    }
}
