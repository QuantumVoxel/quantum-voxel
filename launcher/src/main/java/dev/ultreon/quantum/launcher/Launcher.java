package dev.ultreon.quantum.launcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pre-main class for quantum.
 * <p style="color: red;">NOTE: Internal API!</p>
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public final class Launcher {
    private static Path dataPath;

    /**
     * Production main method.
     * <p style="color: red;">NOTE: Internal API!</p>
     *
     * @param args Arguments to pass to the game.
     */
    public static void main(String[] args) {
        if (OS.isWindows()) {
            dataPath = Path.of(System.getenv("APPDATA"), "UltreonStudios/QuantumVoxel");
        } else if (OS.isMac()) {
            dataPath = Path.of(System.getProperty("user.home"), "Library/Application Support/dev.ultreon.quantumvoxel");
        } else if (OS.isLinux()) {
            dataPath = Path.of(System.getProperty("user.home"), ".config/dev.ultreon.quantum-voxel");
        } else {
            System.exit(255);
            return;
        }

        if (Files.notExists(dataPath)) {
            try {
                Files.createDirectories(dataPath);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(254);
            }
        }

        try {
            System.setOut(new PrintStream(Files.newOutputStream(dataPath.resolve("launcher.log"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE), true));
            System.setErr(new PrintStream(Files.newOutputStream(dataPath.resolve("launcher_err.log"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE), true));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(248);
            return;
        }

        try {
            System.out.println("Launching Quantum Voxel...");
            launch(args);
        } catch (Exception t) {
            t.printStackTrace();
            System.exit(253);
        }

    }

    private static void launch(String[] args) {
        List<String> argv = new ArrayList<>(Arrays.asList(args));

        if (argv.remove("--debug")) System.setProperty("fabric.log.level", "debug");
        if (argv.remove("--server")) System.setProperty("fabric.side", "server");
        else System.setProperty("fabric.side", "client");

        System.setProperty("log4j2.formatMsgNoLookups", "true");
        System.setProperty("fabric.log.disableAnsi", "true");

        args = argv.toArray(new String[0]);

        // Launch game in data directory with classpath relative to the current jar file
        try {
            Path jarPath = getJarPath();
            System.out.println("Launching game from " + jarPath.toAbsolutePath());
            Path libsPath = jarPath.resolveSibling("app");
            System.out.println("Game libs at: " + libsPath.toAbsolutePath());

            List<String> collect = new ArrayList<>();
            try (Stream<Path> list = Files.list(libsPath)) {
                for (Path path : list.collect(Collectors.toList())) {
                    if (path.getFileName().toString().endsWith(".jar")) {
                        collect.add(path.toAbsolutePath().toString());
                    }
                }
            }

            System.out.println("Found " + collect.size() + " libraries!");
            Path javaPath = jarPath.resolveSibling("jdk");
            switch (OS.getOSType()) {
                case Linux:
                case Mac:
                    List<Path> bin;
                    try (Stream<Path> list = Files.list(javaPath.resolve("bin"))) {
                        bin = list.collect(Collectors.toList());
                    }
                    for (Path path : bin) {
                        Files.setAttribute(path, "posix:permissions", PosixFilePermissions.fromString("rwxr-xr-x"));
                    }
                    javaPath = javaPath.resolve("bin/java");
                    break;
                case Windows:
                    javaPath = javaPath.resolve("bin/java.exe");
                    break;
                default:
                    System.exit(249);
                    return;
            }


            List<String> argsFinal = new ArrayList<>();
            argsFinal.add(javaPath.toAbsolutePath().toString());
            argsFinal.add("-XX:+UnlockExperimentalVMOptions");
            argsFinal.add("-XX:+UseZGC");
            argsFinal.add("-Djava.library.path=" + jarPath.getParent().resolveSibling("natives").toAbsolutePath());
            argsFinal.add("-Dfabric.log.disableAnsi=true");
            argsFinal.add("-Dfabric.skipMcProvider=true");
            argsFinal.add("-Dfabric.development=false");
            argsFinal.add("-cp");
            argsFinal.add(String.join(File.pathSeparator, collect));
            argsFinal.add("net.fabricmc.loader.launch.knot.KnotClient");
            argsFinal.addAll(Arrays.asList(args));

            System.out.println("Launching game with command line: " + String.join(" ", argsFinal));

            Process exec = new ProcessBuilder(argsFinal.toArray(String[]::new))
                    .directory(dataPath.toFile())
                    .inheritIO()
                    .start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Shutting down launcher...");
                    exec.destroyForcibly();
                } catch (Throwable ignored) {
                }
            }));
            int exitCode = exec.waitFor();
            if (exitCode != 0) {
                System.out.println("Game exited with code " + exitCode);
                System.exit(exitCode);
                return;
            }
            System.out.println("Game exited with code 0");
            System.exit(0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(252);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(251);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(250);
        }
    }

    public static Path getJarPath() throws URISyntaxException {
        return new File(Launcher.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()).toPath();
    }
}
