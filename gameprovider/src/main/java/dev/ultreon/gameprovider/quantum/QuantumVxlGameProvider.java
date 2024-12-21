package dev.ultreon.gameprovider.quantum;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.GameProviderHelper;
import net.fabricmc.loader.impl.game.LibClassifier;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is the main entry point for the game provider of quantum.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue", "unused"})
public class QuantumVxlGameProvider implements GameProvider {
    private static final String[] ALLOWED_EARLY_CLASS_PREFIXES = {"org.apache.logging.log4j.", "dev.ultreon.gameprovider.quantum.", "dev.ultreon.premain."};

    private final GameTransformer transformer = new GameTransformer();
    private EnvType Env;
    private Arguments arguments;
    private final List<Path> gameJars = new ArrayList<>();
    private final List<Path> logJars = new ArrayList<>();
    private final List<Path> miscGameLibraries = new ArrayList<>();
    private Collection<Path> validParentClassPath = new ArrayList<>();
    private String entrypoint;
    private boolean log4jAvailable;
    private boolean slf4jAvailable;
    private Path libGdxJar;
    private final Properties versions;

    /**
     * Constructor for QuantumVxlGameProvider class.
     * Reads version properties from a file and initializes the versions' property.
     */
    public QuantumVxlGameProvider() {
        // Load version properties from versions.properties file
        InputStream stream = this.getClass().getResourceAsStream("/versions.properties");

        Properties properties = new Properties();

        try {
            // Load properties from the input stream
            properties.load(stream);
        } catch (IOException e) {
            // Throw a runtime exception if there is an issue with loading properties
            throw new RuntimeException(e);
        }

        // Set the versions property to the loaded properties
        this.versions = properties;
    }

    /**
     * Get the game ID.
     *
     * @return the game ID
     */
    @Override
    public String getGameId() {
        return "quantum";
    }

    /**
     * Get the game name.
     *
     * @return the game name
     */
    @Override
    public String getGameName() {
        return "Quantum Voxel";
    }

    /**
     * Get the raw game version.
     *
     * @return the raw game version
     */
    @Override
    public String getRawGameVersion() {
        return this.versions.getProperty("quantum");
    }

    /**
     * Get the normalized game version.
     *
     * @return the normalized game version
     */
    @Override
    public String getNormalizedGameVersion() {
        return this.versions.getProperty("quantum");
    }

    /**
     * Retrieves a collection of BuiltinMods.
     *
     * @return collection of BuiltinMods
     */
    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        return List.of(
                // Creating a BuiltinMod for LibGDX
                new BuiltinMod(List.of(this.libGdxJar), new BuiltinModMetadata.Builder("gdx", this.versions.getProperty("gdx"))
                        .setName("LibGDX")
                        .setDescription("""
                                LibGDX is a Java game development framework for
                                 creating games across multiple platforms.
                                It simplifies the development process with
                                 cross-platform capabilities, high-performance rendering,
                                 and a large community.
                                """)
                        .addLicense("Apache-2.0")
                        .addAuthor("libGDX", Map.of("homepage", "http://www.libgdx.com/", "patreon", "https://patreon.com/libgdx", "github", "https://github.com/libgdx", "sources", "https://github.com/libgdx/libgdx"))
                        .addAuthor("Mario Zechner", Map.of("github", "https://github.com/badlogic", "email", "badlogicgames@gmail.com"))
                        .addAuthor("Nathan Sweet", Map.of("github", "https://github.com/NathanSweet", "email", "nathan.sweet@gmail.com"))
                        .addIcon(200, "assets/gdx/icon.png")
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("log4j", this.versions.getProperty("log4j"))
                        .setName("Apache Log4J")
                        .setDescription("""
                                Apache Log4J is an open source
                                 logging framework for Java.
                                """)
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("homepage", "https://logging.apache.org/log4j/", "sources", "https://github.com/apache/logging-log4j2")))
                        .addAuthor("The Apache Software Foundation", Map.of("homepage", "https://www.apache.org/", "sources", "https://github.com/apache/logging-log4j2"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("apache-commons-collections4", this.versions.getProperty("apache-commons-collections4"))
                        .setName("Apache Commons Collections")
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://github.com/apache/commons-collections")))
                        .addAuthor("The Apache Software Foundation", Map.of("homepage", "https://www.apache.org/", "sources", "https://github.com/apache/commons-collections"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("apache-commons-compress", this.versions.getProperty("apache-commons-compress"))
                        .setName("Apache Commons Compress")
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://github.com/apache/commons-compress")))
                        .addAuthor("The Apache Software Foundation", Map.of("homepage", "https://www.apache.org/", "sources", "https://github.com/apache/commons-compress"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("apache-commons-lang3", this.versions.getProperty("apache-commons-lang3"))
                        .setName("Apache Commons Lang")
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://github.com/apache/commons-lang")))
                        .addAuthor("The Apache Software Foundation", Map.of("homepage", "https://www.apache.org/", "sources", "https://github.com/apache/commons-lang"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("slf4j", this.versions.getProperty("slf4j"))
                        .setName("SLF4J")
                        .setDescription("""
                                SLF4J is a simple logging API for Java.
                                It is designed to be simple to use,
                                 but flexible enough to be a ServiceProvider.
                                """)
                        .addLicense("MIT")
                        .addAuthor("Q-OS.CH", Map.of("homepage", "https://q-os.ch/", "sources", "https://github.com/qos-ch/slf4j"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("gdx-controllers", this.versions.getProperty("gdx-controllers"))
                        .setName("LibGDX Controllers")
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://github.com/libgdx/gdx-controllers")))
                        .addAuthor("libGDX", Map.of("homepage", "http://libgdx.com/", "sources", "https://github.com/libgdx"))
                        .setEnvironment(ModEnvironment.CLIENT)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("gdx-ai", this.versions.getProperty("gdx-ai"))
                        .setName("LibGDX AI")
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://github.com/libgdx/gdx-ai")))
                        .addAuthor("libGDX", Map.of("homepage", "http://libgdx.com/", "sources", "https://github.com/libgdx"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("asm", this.versions.getProperty("asm"))
                        .setName("ObjectWeb ASM")
                        .setDescription("")
                        .addLicense("MIT")
                        .addAuthor("ObjectWeb", Map.of("homepage", "https://asm.ow2.io/", "sources", "https://gitlab.ow2.org/asm/asm"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(List.of(), new BuiltinModMetadata.Builder("vis-ui", this.versions.getProperty("visui"))
                        .setName("VisUI")
                        .setDescription("")
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://github.com/kotcrab/vis-ui")))
                        .addAuthor("Kotcrab", Map.of("homepage", "https://github.com/kotcrab/vis-ui", "github", "https://github.com/kotcrab"))
                        .setEnvironment(ModEnvironment.CLIENT)
                        .build()),
                // Creating a BuiltinMod for Quantum Voxel
                new BuiltinMod(this.gameJars, new BuiltinModMetadata.Builder("ubo", this.versions.getProperty("ubo"))
                        .setName("UBO Data API")
                        .addLicense("Apache-2.0")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://gitlab.com/ultreon/ubo", "issues", "https://gitlab.com/ultreon/ubo/issues")))
                        .addAuthor("Ultreon Development Studios", Map.of("github", "https://github.com/Ultreon", "gitlab", "https://gitlab.com/ultreon", "email", "contact@ultreon.dev"))
                        .addContributor("XyperCode", Map.of("github", "https://github.com/XyperCode", "gitlab", "https://gitlab.com/XyperCode", "email", "xypercode@ultreon.dev"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),
                new BuiltinMod(this.gameJars, new BuiltinModMetadata.Builder("corelibs", this.versions.getProperty("corelibs"))
                        .setName("Ultreon Core Libraries")
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://gitlab.com/ultreon/corelibs", "issues", "https://gitlab.com/ultreon/corelibs/issues")))
                        .addAuthor("Ultreon Development Studios", Map.of("github", "https://github.com/Ultreon", "gitlab", "https://gitlab.com/ultreon", "email", "contact@ultreon.dev"))
                        .addContributor("XyperCode", Map.of("github", "https://github.com/XyperCode", "gitlab", "https://gitlab.com/XyperCode", "email", "xypercode@ultreon.dev"))
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .build()),

                // Creating a BuiltinMod for Quantum Voxel
                new BuiltinMod(this.gameJars, new BuiltinModMetadata.Builder("quantum", this.versions.getProperty("quantum"))
                        .addLicense("Ultreon-PSL-1.0")
                        .addAuthor("Ultreon Development Studios", Map.of("github", "https://github.com/Ultreon", "gitlab", "https://gitlab.com/ultreon", "email", "contact@ultreon.dev"))
                        .addContributor("XyperCode", Map.of("github", "https://github.com/XyperCode", "gitlab", "https://gitlab.com/XyperCode", "email", "xypercode@ultreon.dev"))
                        .addContributor("MincraftEinstein", Map.of("github", "https://github.com/MincraftEinstein"))
                        .addContributor("Creatomat Gaming", Map.of("github", "https://github.com/Creatomat"))
                        .addIcon(128, "assets/craft/icon.png")
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://gitlab.com/ultreon/quantum/game", "email", "contact.ultreon@gmail.com", "homepage", "https://ultreon.dev/?id=quantum#project", "discord", "https://discord.gg/WePT9v2CmQ", "issues", "https://gitlab.com/ultreon/quantum/game/-/issues")))
                        .setDescription("""
                                A blocky, voxel-based world where you can explore,
                                 build, and survive in a vast and ever-changing environment.
                                Inspired by the best of the voxel game genre.
                                
                                It's also the game that you're playing right now!""")
                        .setName("Quantum Voxel")
                        .build())
        );
    }

    @Override
    public String getEntrypoint() {
        return this.entrypoint;
    }

    @Override
    public Path getLaunchDirectory() {
        return Path.of(".");
    }

    @NotNull
    public static Path getDataDir() {
        Path path;
        if (OS.isWindows())
            path = Paths.get(System.getenv("APPDATA"), "QuantumVoxel");
        else if (OS.isMac())
            path = Paths.get(System.getProperty("user.home"), "Library/Application Support/QuantumVoxel");
        else if (OS.isLinux())
            path = Paths.get(System.getProperty("user.home"), ".config/QuantumVoxel");
        else
            throw new FormattedException("Unsupported Platform", "Platform unsupported: " + System.getProperty("os.name"));

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return path;
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Locates the Quantum Voxel game.
     *
     * @param launcher the Fabric launcher
     * @param args     the game arguments
     * @return {@code true} if the game was located, {@code false} otherwise
     */
    @Override
    public boolean locateGame(FabricLauncher launcher, String[] args) {
        // Set the environment type and parse the arguments
        this.Env = launcher.getEnvironmentType();
        this.arguments = new Arguments();
        this.arguments.parse(args);

        try {
            // Create a new LibClassifier object with the specified class and environment type
            var classifier = new LibClassifier<>(GameLibrary.class, this.Env, this);

            // Get the client and server libraries
            var clientLib = GameLibrary.QUANTUM_VXL_CLIENT;
            var serverLib = GameLibrary.QUANTUM_VXL_SERVER;

            // Get the common game jar and check if it is declared
            var clientJar = GameProviderHelper.getCommonGameJar();
            var commonGameJarDeclared = clientJar != null;

            // Process the common game jar if it is declared
            if (commonGameJarDeclared) {
                classifier.process(clientJar);
            }
            List<Exception> suppressedExceptions = new ArrayList<>();

            // Process the launcher class path
            classifier.process(launcher.getClassPath());

            for (Path path : launcher.getClassPath()) {
                System.out.println(path);
            }

            // Get the client and server jars from the classifier
            clientJar = classifier.getOrigin(GameLibrary.QUANTUM_VXL_CLIENT);
            var serverJar = classifier.getOrigin(GameLibrary.QUANTUM_VXL_SERVER);
            this.libGdxJar = classifier.getOrigin(GameLibrary.LIBGDX);

            // Warn if the common game jar didn't contain any of the expected classes
            if (commonGameJarDeclared && clientJar == null) {
                Log.warn(LogCategory.GAME_PROVIDER, "The declared common game jar didn't contain any of the expected classes!");
                suppressedExceptions.add(new FormattedException("The declared common game jar didn't contain any of the expected classes!", "The declared common game jar didn't contain any of the expected classes!"));
            }

            // Add the client and server jars to the game jars list
            if (clientJar != null) {
                this.gameJars.add(clientJar);
            } else {
                suppressedExceptions.add(new FormattedException("No client jar found", "No client jar found for Quantum Voxel"));
            }
            if (serverJar != null) {
                this.gameJars.add(serverJar);
            } else {
                suppressedExceptions.add(new FormattedException("No server jar found", "No server jar found for Quantum Voxel"));
            }
            if (this.libGdxJar != null) {
                this.gameJars.add(this.libGdxJar);
            } else {
                suppressedExceptions.add(new FormattedException("No libgdx jar found", "No libgdx jar found for Quantum Voxel"));
            }

            if (this.gameJars.isEmpty()) {
                if (!suppressedExceptions.isEmpty()) {
                    FormattedException noGameJarFound = new FormattedException("No game jar found", "No game jar found for Quantum Voxel");

                    for (Exception e : suppressedExceptions) {
                        noGameJarFound.addSuppressed(e);
                    }
                    throw noGameJarFound;
                }
                throw new FormattedException("No game jar found", "No game jar found for Quantum Voxel");
            }

            // Get the entry point class name from the classifier
            this.entrypoint = classifier.getClassName(clientLib);
            if (this.entrypoint == null) {
                this.entrypoint = classifier.getClassName(serverLib);
            }

            // Check if log4j and slf4j are available
            this.log4jAvailable = classifier.has(GameLibrary.LOG4J_API) && classifier.has(GameLibrary.LOG4J_CORE);
            this.slf4jAvailable = classifier.has(GameLibrary.SLF4J_API) && classifier.has(GameLibrary.SLF4J_CORE);
            var hasLogLib = this.log4jAvailable || this.slf4jAvailable;

            // Configure the built-in log
            Log.configureBuiltin(hasLogLib, !hasLogLib);

            // Add logging jars to the appropriate lists
            for (var lib : GameLibrary.LOGGING) {
                var path = classifier.getOrigin(lib);

                if (path != null) {
                    if (hasLogLib) {
                        this.logJars.add(path);
                    } else if (!this.gameJars.contains(path)) {
                        this.miscGameLibraries.add(path);
                    }
                }
            }

            // Add unmatched origins to the misc game libraries list
            this.miscGameLibraries.addAll(classifier.getUnmatchedOrigins());

            // Get the valid parent class path from the classifier
            this.validParentClassPath = classifier.getSystemLibraries();

        } catch (IOException e) {
            // Wrap and throw the exception
            throw ExceptionUtil.wrap(e);
        }

        // Expose game jar locations to the FabricLoader share
        var share = FabricLoaderImpl.INSTANCE.getObjectShare();

        share.put("fabric-loader:inputGameJar", this.gameJars.getFirst());
        share.put("fabric-loader:inputGameJars", this.gameJars);

        return true;
    }

    /**
     * Initializes the FabricLauncher with necessary configurations.
     *
     * @param launcher The FabricLauncher instance to initialize
     */
    @Override
    public void initialize(FabricLauncher launcher) {
        // Set the valid parent class path
        launcher.setValidParentClassPath(this.validParentClassPath);

        // Load the logger libraries on the platform CL when not in a unit test
        if (!this.logJars.isEmpty() && !Boolean.getBoolean(SystemProperties.UNIT_TEST)) {
            for (var jar : this.logJars) {
                if (this.gameJars.contains(jar)) {
                    launcher.addToClassPath(jar, QuantumVxlGameProvider.ALLOWED_EARLY_CLASS_PREFIXES);
                } else {
                    launcher.addToClassPath(jar);
                }
            }
        }

        // Setup the log handler
        this.setupLogHandler(launcher, true);

        // Locate entry points using the transformer
        this.transformer.locateEntrypoints(launcher, new ArrayList<>());
    }

    /**
     * Sets up the log handler for the Fabric launcher.
     *
     * @param launcher    the Fabric launcher instance
     * @param useTargetCl true if the target class loader should be used, false otherwise
     */
    private void setupLogHandler(FabricLauncher launcher, boolean useTargetCl) {
        // Disable lookups as they are not used by Quantum Voxel and can cause issues with older log4j2 versions
        System.setProperty("log4j2.formatMsgNoLookups", "true");

        try {
            // Specify the class name for the custom log handler
            final var logHandlerClsName = "dev.ultreon.gameprovider.quantum.QuantumVxlLogHandler";

            // Save the previous class loader
            var prevCl = Thread.currentThread().getContextClassLoader();
            Class<?> logHandlerCls;

            // Depending on the flag, use the target class loader or load the class directly
            if (useTargetCl) {
                Thread.currentThread().setContextClassLoader(launcher.getTargetClassLoader());
                logHandlerCls = launcher.loadIntoTarget(logHandlerClsName);
            } else {
                logHandlerCls = Class.forName(logHandlerClsName);
            }

            // Initialize the log handler with the instantiated class
            Log.init((LogHandler) logHandlerCls.getConstructor().newInstance());
            // Restore the previous class loader
            Thread.currentThread().setContextClassLoader(prevCl);
        } catch (ReflectiveOperationException e) {
            // Throw a runtime exception if there is a reflective operation exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return this.transformer;
    }

    @Override
    public boolean hasAwtSupport() {
        return false;
    }

    /**
     * Unlocks the class path for the given FabricLauncher by setting allowed prefixes for gameJars and adding miscGameLibraries to the classpath.
     *
     * @param launcher the FabricLauncher instance
     */
    @Override
    public void unlockClassPath(FabricLauncher launcher) {
        // Set allowed prefixes for gameJars that are logged
        for (var gameJar : this.gameJars) {
            if (this.logJars.contains(gameJar)) {
                launcher.setAllowedPrefixes(gameJar);
            } else {
                launcher.addToClassPath(gameJar);
            }
        }

        // Add miscGameLibraries to the classpath
        for (var lib : this.miscGameLibraries) {
            launcher.addToClassPath(lib);
        }
    }

    /**
     * Returns the first game jar from the list of game jars.
     *
     * @return the first game jar
     */
    public Path getGameJar() {
        return this.gameJars.get(0);
    }

    /**
     * Launches the application using the provided ClassLoader.
     * Sets the user directory to the launch directory.
     * Loads the target class and invokes its main method with the specified arguments.
     *
     * @param loader The ClassLoader to use for loading the target class.
     */
    @Override
    public void launch(ClassLoader loader) {
        // Get the target class to launch
        var targetClass = this.entrypoint;

        // Set the user directory to the launch directory
        Path launchDirectory = this.getLaunchDirectory();
        String absolutePath = launchDirectory.toFile().getAbsolutePath();
        System.setProperty("user.dir", absolutePath);

        MethodHandle invoker;

        try {
            // Load the target class and find the 'main' method handle
            var c = loader.loadClass(targetClass);
            invoker = MethodHandles.lookup().findStatic(c, "main", MethodType.methodType(void.class, String[].class));
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            throw new FormattedException("Failed to start Quantum Voxel", e);
        }

        try {
            //noinspection ConfusingArgumentToVarargsMethod
            invoker.invokeExact(this.arguments.toArray());
        } catch (Throwable t) {
            throw new FormattedException("Quantum Voxel has crashed", t);
        }
    }

    /**
     * Get the arguments for the method.
     *
     * @return the arguments
     */
    @Override
    public Arguments getArguments() {
        return this.arguments;
    }

    /**
     * Check if the error GUI can be opened.
     *
     * @return true if the error GUI can be opened, false otherwise
     */
    @Override
    public boolean canOpenErrorGui() {
        if (this.arguments == null || this.Env == Env.CLIENT)
            return !OS.isMobile();

        return false;
    }

    /**
     * Get the launch arguments.
     *
     * @param sanitize flag to indicate if the arguments should be sanitized
     * @return an array of launch arguments
     */
    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }
}
