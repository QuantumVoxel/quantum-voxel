package dev.ultreon.quantum.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import dev.ultreon.quantum.*;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.desktop.DesktopLogger.Slf4jLogger;
import dev.ultreon.quantum.desktop.imgui.ImGuiOverlay;
import dev.ultreon.quantum.log.Logger;
import dev.ultreon.quantum.util.Env;
import dev.ultreon.quantum.util.Result;
import dev.ultreon.xeox.loader.XeoxLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.Platform;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static dev.ultreon.quantum.client.QuantumClient.crash;

public abstract class DesktopPlatform extends GamePlatform {
    private final Map<String, FabricMod> mods = new IdentityHashMap<>();

    DesktopPlatform() {
        super();
    }

    @Override
    public void preInitImGui() {
        ImGuiOverlay.preInitImGui();
    }

    @Override
    public void setupImGui() {
        ImGuiOverlay.setupImGui();
    }

    @Override
    public void renderImGui() {
        ImGuiOverlay.renderImGui(QuantumClient.get());
    }

    @Override
    public void onFirstRender() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();
        window.setVisible(true);
    }

    @Override
    public void onGameDispose() {
        ImGuiOverlay.dispose();
    }

    @Override
    public boolean isShowingImGui() {
        return ImGuiOverlay.isShown();
    }

    @Override
    public void setShowingImGui(boolean value) {
        ImGuiOverlay.setShowingImGui(value);
    }

    @Override
    public boolean areChunkBordersVisible() {
        return ImGuiOverlay.isChunkSectionBordersShown();
    }

    @Override
    public boolean showRenderPipeline() {
        return ImGuiOverlay.SHOW_RENDER_PIPELINE.get();
    }

    @Override
    public Optional<Mod> getMod(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(container -> (Mod) this.mods.computeIfAbsent(id, v -> new FabricMod(container))).or(() -> super.getMod(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id) || super.isModLoaded(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        var list = new ArrayList<Mod>();
        list.addAll(FabricLoader.getInstance().getAllMods().stream().map(container -> this.mods.computeIfAbsent(container.getMetadata().getId(), v -> new FabricMod(container))).toList());
        list.addAll(super.getMods());
        return list;
    }

    @Override
    public boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        FabricLoader.getInstance().invokeEntrypoints(name, initClass, init);
    }

    @Override
    public Env getEnv() {
        switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT:
                return Env.CLIENT;
            case SERVER:
                return Env.SERVER;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Result<Boolean> openImportDialog() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(true);
        int result = jFileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = jFileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                return XeoxLoader.get().importMod(file).map(v -> true, v -> v);
            }
            return Result.ok(false);
        }
        return Result.ok(false);
    }

    @Override
    public boolean isDesktop() {
        return true;
    }

    @Override
    public void locateResources() {try {
        URL resource = QuantumClient.class.getResource("/.ucraft-resources");
        if (resource == null) {
            throw new GdxRuntimeException("Quantum Voxel resources unavailable!");
        }
        String string = resource.toString();

        if (string.startsWith("jar:")) {
            string = string.substring("jar:".length());
        }

        string = string.substring(0, string.lastIndexOf('/'));

        if (string.endsWith("!")) {
            string = string.substring(0, string.length() - 1);
        }

        QuantumClient.get().getResourceManager().importPackage(new File(new URI(string)).toPath());
    } catch (Exception e) {
        for (Path rootPath : FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getRootPaths()) {
            try {
                QuantumClient.get().getResourceManager().importPackage(rootPath);
            } catch (IOException ex) {
                crash(ex);
            }
        }
    }
    }

    @Override
    public void locateModResources() {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getOrigin().getKind() != ModOrigin.Kind.PATH) continue;

            for (Path rootPath : mod.getRootPaths()) {
                // Try to import a resource package for the given mod path.
                try {
                    QuantumClient.get().getResourceManager().importPackage(rootPath);
                } catch (IOException e) {
                    CommonConstants.LOGGER.warn("Importing resources failed for path: " + rootPath.toFile(), e);
                }
            }
        }
    }

    @Override
    public boolean isMacOSX() {
        return Platform.get() == Platform.MACOSX;
    }

    @Override
    public boolean isWindows() {
        return Platform.get() == Platform.WINDOWS;
    }

    @Override
    public boolean isLinux() {
        return Platform.get() == Platform.LINUX;
    }

    @Override
    @Deprecated
    public void setupMacOSX() {
        if (isMacOSX()) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
            Configuration.GLFW_CHECK_THREAD0.set(false);
        }
    }

    @Override
    public void launch(String[] argv) {

    }

    @Override
    public void close() {
        ((Lwjgl3Graphics) Gdx.graphics).getWindow().closeWindow();
    }

    @Override
    public void setVisible(boolean visible) {
        ((Lwjgl3Graphics) Gdx.graphics).getWindow().setVisible(visible);
    }

    @Override
    public void requestAttention() {
        ((Lwjgl3Graphics) Gdx.graphics).getWindow().flash();
    }

    @Override
    public Logger getLogger(String name) {
        return new Slf4jLogger(LoggerFactory.getLogger(name));
    }

    @Override
    public boolean detectDebug() {
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        boolean debugFlagPresent = args.contains("-Xdebug");
        boolean jdwpPresent = args.toString().contains("jdwp");
        return debugFlagPresent || jdwpPresent;
    }

    public abstract GameWindow createWindow();

    @Override
    public boolean isMouseCaptured() {
        return Gdx.input.isCursorCatched();
    }

    @Override
    public void setMouseCaptured(boolean captured) {
        Gdx.input.setCursorCatched(captured);
    }

    @Override
    public void setCursorPosition(int x, int y) {
        Gdx.input.setCursorPosition(x, y);
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.DESKTOP;
    }

    @Override
    public void setTransparentFBO(boolean enable) {
//        GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, enable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
    }
}
