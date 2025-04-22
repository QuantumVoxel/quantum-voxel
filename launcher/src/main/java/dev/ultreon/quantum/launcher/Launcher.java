package dev.ultreon.quantum.launcher;

import com.google.common.collect.Lists;
import dev.ultreon.gameprovider.quantum.OS;
import dev.ultreon.libs.commons.v0.util.ExceptionUtils;
import net.fabricmc.loader.impl.launch.knot.KnotClient;
import org.jetbrains.annotations.ApiStatus;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;

/**
 * Pre-main class for quantum.
 * <p style="color: red;">NOTE: Internal API!</p>
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@ApiStatus.Internal
public final class Launcher {
    /**
     * Production main method.
     * <p style="color: red;">NOTE: Internal API!</p>
     *
     * @param args Arguments to pass to the game.
     */
    @ApiStatus.Internal
    public static void main(String[] args) {
        try {
            List<String> argv = Lists.newArrayList(args);
            System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");

            if (OS.isWindows()) {
                try {
                    System.setOut(new PrintStream(Files.newOutputStream(Path.of("launcher.log"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)));
                    System.setErr(new PrintStream(Files.newOutputStream(Path.of("launcher_err.log"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (argv.remove("--debug")) System.setProperty("fabric.log.level", "debug");
            if (argv.remove("--server")) System.setProperty("fabric.side", "server");
            else System.setProperty("fabric.side", "client");

            System.setProperty("log4j2.formatMsgNoLookups", "true");
            System.setProperty("fabric.log.disableAnsi", "true");

            // Copy mixinprovider.jar to ./mods/
            try {
                if (!Files.exists(Path.of("mods/")))
                    Files.createDirectory(Path.of("mods/"));
                Files.copy(Objects.requireNonNull(Launcher.class.getResourceAsStream("/mixinprovider.jar"), "mixinprovider.jar"), Path.of("mods/mixinprovider.jar"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                Runtime.getRuntime().exit(1);
            }

            args = argv.toArray(new String[0]);
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, t.getMessage() + "\n" + ExceptionUtils.getStackTrace(t).replace("\t", "    "), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(255);
        }
        KnotClient.main(args);
    }

}
