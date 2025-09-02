package dev.ultreon.quantum.desktop;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Documentation;
import ch.njol.skript.doc.HTMLGenerator;
import ch.njol.skript.test.runner.TestMode;
import dev.ultreon.baseskript.BaseSkript;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.GameWindow;
import dev.ultreon.quantum.client.skript.QuantumClientSkript;
import dev.ultreon.quantum.platform.Device;
import dev.ultreon.quantum.platform.MouseDevice;
import dev.ultreon.quantum.skript.QuantumSkript;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SkriptDocsGenMain {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar SkriptDocsGenMain.jar <template-dir> <output>");
            System.err.println("Arguments:");
            System.err.println("  <template-dir> - The directory containing the template files");
            System.err.println("  <output> - The output directory");
            System.exit(1);
        }

        new DesktopPlatform(false, new SafeLoadWrapper(args)) {
            @Override
            public GameWindow createWindow() {
                return null;
            }

            @Override
            public @Nullable MouseDevice getMouseDevice() {
                return null;
            }

            @Override
            public Collection<Device> getGameDevices() {
                return List.of();
            }
        };

        QuantumSkript quantumSkript = new QuantumSkript(() -> null);
        QuantumClientSkript quantumClientSkript = new QuantumClientSkript();

        BaseSkript.load();
        quantumSkript.onLoad();
        quantumSkript.onEnable();
        quantumClientSkript.onLoad();
        quantumClientSkript.onEnable();

        BaseSkript.init();

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            File templateDir = Documentation.getDocsTemplateDirectory();
            File outputDir = Documentation.getDocsOutputDirectory();
            outputDir.mkdirs();

            Skript.info("Generating docs at: " + outputDir.getAbsolutePath());
            Skript.info("Using template at: " + templateDir.getAbsolutePath());

            if (!templateDir.exists()) {
                Skript.info("JSON-only documentation generated!");
                return;
            }

            HTMLGenerator htmlGenerator = new HTMLGenerator(templateDir, outputDir);
            htmlGenerator.generate(); // Try to generate docs... hopefully
            Skript.info("All documentation generated!");
        });
    }
}
