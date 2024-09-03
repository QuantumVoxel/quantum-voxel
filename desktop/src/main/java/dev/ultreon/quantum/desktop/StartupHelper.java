/*
 * Copyright 2020 damios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//Note, the above license and copyright applies to this file only.
//Another note: this file has changed by XyperCode. And differs from the original.

package dev.ultreon.quantum.desktop;

import com.google.gson.Gson;
import oshi.SystemInfo;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

/**
 * Adds some utilities to ensure that the JVM was started with the
 * {@code -XstartOnFirstThread} argument, which is required on macOS for LWJGL 3
 * to function. Also helps on Windows when users have names with characters from
 * outside the Latin alphabet, a common cause of startup crashes.
 * <br>
 * <a href="https://jvm-gaming.org/t/starting-jvm-on-mac-with-xstartonfirstthread-programmatically/57547">Based on this java-gaming.org post by kappa</a>
 * @author damios
 */
public class StartupHelper {

    private static final String JVM_RESTARTED_ARG = "jvmIsRestarted";
    public static final long MB = 1048576;

    private StartupHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts a new JVM if the application was started on macOS without the
     * {@code -XstartOnFirstThread} argument. This also includes some code for
     * Windows, for the case where the user's home directory includes certain
     * non-Latin-alphabet characters (without this code, most LWJGL3 apps fail
     * immediately for those users). Returns whether a new JVM was started and
     * thus no code should be executed.
     * <p>
     * <u>Usage:</u>
     *
     * <pre><code>
     * public static void main(String... args) {
     * 	if (StartupHelper.startNewJvmIfRequired(true)) return; // This handles macOS support and helps on Windows.
     * 	// after this is the actual main method code
     * }
     * </code></pre>
     *
     * @param redirectOutput whether the output of the new JVM should be rerouted to the
     *                       old JVM, so it can be accessed in the same place; keeps the
     *                       old JVM running if enabled
     * @return whether a new JVM was started and thus no code should be executed
     * in this one
     */
    public static boolean startNewJvmIfRequired(boolean redirectOutput) {
        return startNewJvmIfRequired(redirectOutput, null);
    }

    /**
     * Starts a new JVM if the application was started on macOS without the
     * {@code -XstartOnFirstThread} argument. This also includes some code for
     * Windows, for the case where the user's home directory includes certain
     * non-Latin-alphabet characters (without this code, most LWJGL3 apps fail
     * immediately for those users). Returns whether a new JVM was started and
     * thus no code should be executed.
     * <p>
     * <u>Usage:</u>
     *
     * <pre><code>
     * public static void main(String... args) {
     * 	if (StartupHelper.startNewJvmIfRequired(true)) return; // This handles macOS support and helps on Windows.
     * 	// after this is the actual main method code
     * }
     * </code></pre>
     *
     * @param redirectOutput
     *            whether the output of the new JVM should be rerouted to the
     *            old JVM, so it can be accessed in the same place; keeps the
     *            old JVM running if enabled
     * @return whether a new JVM was started and thus no code should be executed
     *         in this one
     */
    public static boolean startNewJvmIfRequired(boolean redirectOutput, Path launcherPath) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (launcherPath == null && !osName.contains("mac")) {
            if (osName.contains("windows")) {
// Here, we are trying to work around an issue with how LWJGL3 loads its extracted .dll files.
// By default, LWJGL3 extracts to the directory specified by "java.io.tmpdir", which is usually the user's home.
// If the user's name has non-ASCII (or some non-alphanumeric) characters in it, that would fail.
// By extracting to the relevant "ProgramData" folder, which is usually "C:\ProgramData", we avoid this.
                System.setProperty("java.io.tmpdir", System.getenv("ProgramData") + "/libGDX-temp");
            }
            System.out.println("No JVM required.");
            return false;
        }

        // There is no need for -XstartOnFirstThread on Graal native image
        if (launcherPath == null && !System.getProperty("org.graalvm.nativeimage.imagecode", "").isEmpty()) {
            System.out.println("No JVM required.");
            return false;
        }

        ProcessHandle current = ProcessHandle.current();
        ProcessHandle.Info procInfo = current.info();
        long pid = current.pid();

        // check whether -XstartOnFirstThread is enabled
        if (launcherPath == null && "1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid))) {
            System.out.println("No JVM required.");
            return false;
        }

        // check whether the JVM was previously restarted
        // avoids looping, but most certainly leads to a crash
        if (launcherPath == null && "true".equals(System.getProperty(JVM_RESTARTED_ARG))) {
            System.err.println("There was a problem evaluating whether the JVM was started with the -XstartOnFirstThread argument.");
            return false;
        }

        // Restart the JVM with -XstartOnFirstThread
        ArrayList<String> jvmArgs = new ArrayList<>();
        String separator = System.getProperty("file.separator");
        // The following line is used assuming you target Java 8, the minimum for LWJGL3.
//        String javaExecPath = System.getProperty("java.home") + separator + "bin" + separator + "java";
        // If targeting Java 9 or higher, you could use the following instead of the above line:
        String javaExecPath = procInfo.command().orElseThrow();

        if (!new File(javaExecPath).exists()) {
            System.err.println("A Java installation could not be found. If you are distributing this app with a bundled JRE, be sure to set the -XstartOnFirstThread argument manually!");
            return false;
        }

        if (launcherPath != null) {
            try {
                if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("linux")) {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                } else {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     UnsupportedLookAndFeelException e) {
                throw new RuntimeException(e);
            }

            if (System.getProperty("os.name").toLowerCase().contains("mac"))
                jvmArgs.add(new File("runtime/Contents/Home/bin/java").getAbsolutePath());
            else if (osName.contains("windows"))
                jvmArgs.add(new File("runtime/bin/java.exe").getAbsolutePath());
            else if (osName.contains("linux")) {
                Path file = Path.of(javaExecPath).toAbsolutePath().getParent().resolve("lib", "runtime", "bin", "java");
                if (Files.notExists(file))
                    file = Path.of("lib/runtime/bin/java");
                if (Files.notExists(file))
                    file = Path.of("../lib/runtime/bin/java");
                if (Files.notExists(file))
                    file = Path.of("runtime/bin/java");
                if (Files.notExists(file)) {
                    JFileChooser jFileChooser = new JFileChooser(".");
                    jFileChooser.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            return f.getName().equals("java") || f.isDirectory();
                        }

                        @Override
                        public String getDescription() {
                            return "Java Executable";
                        }
                    });
                    jFileChooser.setDialogTitle("Select Java Executable");
                    if (jFileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                        System.exit(1);
                    }
                }
                jvmArgs.add(file.toAbsolutePath().toString());
            } else
                throw new RuntimeException("Unsupported OS: " + osName);
        } else {
            jvmArgs.add(javaExecPath);
        }
        if (System.getProperty("os.name").toLowerCase().contains("mac")) jvmArgs.add("-XstartOnFirstThread");
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(v -> !v.startsWith("-Xmx") && !v.startsWith("-Xms")).toList());

        Gson gson = new Gson();
        Path configPath = launcherPath.resolve("launch_cfg.json");

        SystemInfo systemInfo = new SystemInfo();

        LaunchConfig config;
        try {
            if (Files.notExists(configPath)) {
                config = new LaunchConfig();
            } else {
                try (BufferedReader json = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                    config = gson.fromJson(json, LaunchConfig.class);
                }
            }
            config.fix(systemInfo);
            try (BufferedWriter writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                gson.toJson(config, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long available = systemInfo.getHardware().getMemory().getAvailable();
        if (config.maxMemoryMB >= (long) (available * 0.9) / MB) {
            int i = JOptionPane.showConfirmDialog(null, "The game is set up the game to use " + config.maxMemoryMB + " MB but there's only " + available / MB + " MB available.\nRecommended amount to have available is 4096 MB.\n\nDo you want to continue?", "Low Memory", JOptionPane.YES_NO_OPTION);
            if (i == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
            config.maxMemoryMB = (long) (available * 0.9) / MB;
        } else if (config.maxMemoryMB < 4096) {
            int i = JOptionPane.showConfirmDialog(null, "There's only " + available / MB + " MB set up for the game.\nRecommended amount to have available is 4096 MB.\n\nDo you want to continue?", "Low Memory", JOptionPane.YES_NO_OPTION);
            if (i == JOptionPane.NO_OPTION)
                System.exit(0);
        }

        jvmArgs.add("-Xmx" + config.maxMemoryMB + "M");
        jvmArgs.add("-Xms" + config.maxMemoryMB + "M");
        jvmArgs.add("-Djava.library.path=" + ManagementFactory.getRuntimeMXBean().getClassPath() + (System.getProperty("os.name").toLowerCase().contains("windows") ? separator + "natives" : ""));
        jvmArgs.add("-Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
        jvmArgs.add("-cp");
        jvmArgs.add(ManagementFactory.getRuntimeMXBean().getClassPath());

        String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
        if (mainClass == null) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            if (trace.length > 0) {
                mainClass = trace[trace.length - 1].getClassName();
            } else {
                System.err.println("The main class could not be determined.");
                return false;
            }
        }
        jvmArgs.add(mainClass);

        // Use @argfile
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                File argFile = File.createTempFile("jvmargs", ".txt");
                try (PrintWriter out = new PrintWriter(argFile)) {
                    for (String arg : jvmArgs) {
                        out.println(arg);
                    }
                }
                jvmArgs.clear();
                jvmArgs.add(javaExecPath);
                jvmArgs.add("@" + argFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        try {
            if (!redirectOutput) {
                ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs).directory(launcherPath != null ? launcherPath.toFile() : new File(".").getAbsoluteFile());
                processBuilder.start();
            } else {
                if (launcherPath != null && Files.notExists(launcherPath)) {
                    Files.createDirectories(launcherPath);
                }
                Process process = new ProcessBuilder(jvmArgs.stream().filter(Objects::nonNull).toArray(String[]::new))
                        .directory(launcherPath != null ? launcherPath.toFile() : new File(".")
                                .getAbsoluteFile())
                        .redirectErrorStream(true)
                        .start();
                BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;

                while ((line = processOutput.readLine()) != null) {
                    System.out.println(line);
                }

                System.exit(process.waitFor());
            }
        } catch (Exception e) {
            System.err.println("There was a problem restarting the JVM");
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Starts a new JVM if the application was started on macOS without the
     * {@code -XstartOnFirstThread} argument. Returns whether a new JVM was
     * started and thus no code should be executed. Redirects the output of the
     * new JVM to the old one.
     * <p>
     * <u>Usage:</u>
     *
     * <pre>
     * public static void main(String... args) {
     * 	if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
     * 	// the actual main method code
     * }
     * </pre>
     *
     * @return whether a new JVM was started and thus no code should be executed
     *         in this one
     */
    public static boolean startNewJvmIfRequired() {
        return startNewJvmIfRequired(true);
    }
}