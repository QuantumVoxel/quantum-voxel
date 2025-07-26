package dev.ultreon.mixinprovider;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.util.Resizer;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.Vec2f;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorCoordinates;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;

import java.util.Objects;

import static dev.ultreon.quantum.desktop.imgui.ImGuiOverlay.*;

public class ImGuiHandler {
    private static final ImBoolean PAUSED = new ImBoolean(false);
    private static QuantumClient client;

    private static final ImBoolean showTextureList = new ImBoolean(false);
    private static final ImBoolean showTextureNode = new ImBoolean(false);

    private static final ImBoolean showTextureAtlasList = new ImBoolean(false);
    private static final ImBoolean showTextureAtlasNode = new ImBoolean(false);

    private static final ImBoolean showMeshList = new ImBoolean(false);
    private static final ImBoolean showMeshNode = new ImBoolean(false);

    private static final ImBoolean showShaderList = new ImBoolean(false);
    private static final ImBoolean showShaderNode = new ImBoolean(false);

    private static final ImBoolean showShaderProgramList = new ImBoolean(false);
    private static final ImBoolean showShaderProgramNode = new ImBoolean(false);

    private static final ImBoolean showModelList = new ImBoolean(false);
    private static final ImBoolean showModelNode = new ImBoolean(false);

    private static final Resizer resizer = new Resizer();

    private static TextureRegion texture;
    private static TextureAtlas textureAtlas;
    private static ShaderProgram shaderProgram;
    private static Shader shader;

    public static void renderMenuBar() {
        if (ImGui.beginMenu("libGDX Debug")) {
            ImGui.menuItem("Texture List", "", showTextureList);
            ImGui.menuItem("Texture Atlas List", "", showTextureAtlasList);
            ImGui.menuItem("Mesh List", "", showMeshList);
            ImGui.menuItem("Shader List", "", showShaderList);
            ImGui.menuItem("Shader Program List", "", showShaderProgramList);
            ImGui.menuItem("Model List", "", showModelList);
            if (ImGui.menuItem((PAUSED.get() ? "Resume" : "Pause") + "##ImGuiHandler::MixinProvider[Pause]")) {
                PAUSED.set(!PAUSED.get());
            }
            ImGui.endMenu();
        }
    }

    public static void renderPreGame() {
    }

    public static void renderWindows(QuantumClient client) {
        ImGuiHandler.client = client;
        if (showTextureList.get()) {
            showTextureListWindow();
        }

        if (showTextureNode.get()) {
            showTextureNodeWindow();
        }
        if (showTextureAtlasList.get()) {
            showTextureAtlasListWindow();
        }
        if (showTextureAtlasNode.get()) {
            showTextureAtlasNodeWindow();
        }
        if (showMeshList.get()) {
            showMeshListWindow();
        }
        if (showMeshNode.get()) {
            showMeshNodeWindow();
        }
        if (showShaderList.get()) {
            showShaderListWindow();
        }
        if (showShaderNode.get()) {
            showShaderNodeWindow();
        }
        if (showShaderProgramList.get()) {
            showShaderProgramListWindow();
        }
        if (showShaderProgramNode.get()) {
            showShaderProgramNodeWindow();
        }
        if (showModelList.get()) {
            showModelListWindow();
        }
        if (showModelNode.get()) {
            showModelNodeWindow();
        }
    }

    private static void showMeshListWindow() {
        if (ImGui.begin("Mesh List")) {
            ImGui.text("Mesh List");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
            if (ImGui.beginListBox("##Meshes{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showMeshNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Mesh Node", showMeshNode)) {
            ImGui.text("Mesh Node");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
        }
        ImGui.end();
    }

    private static void showShaderListWindow() {
        if (ImGui.begin("Shader List")) {
            ImGui.text("Shader List");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
            if (ImGui.beginListBox("##Shaders{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                for (Shader shader : GdxRegistries.SHADERS) {
                    if (ImGui.selectable("Shader ID: " + shader.hashCode(), ImGuiHandler.shader == shader)) {
                        showShaderNode.set(true);
                        ImGuiHandler.shader = shader;
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Shader Classname: " + shader.getClass().getName());
                        ImGui.endTooltip();
                    }
                }
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showSource(ShaderProgram shader, String source, String type) {
        NamespaceID location = NamespaceID.of("mixinprovider/text_editor/" + shader.getHandle() + "/" + type);
        TextEditor textEditor = textEditors.get(location);
        if (textEditor == null) {
            textEditor = new TextEditor();
            textEditors.put(location, textEditor);
        }

        textEditor.setText(source);
        textEditor.setReadOnly(true);
        textEditor.setLanguageDefinition(glsl);
        textEditor.setColorizerEnable(true);
        textEditor.setShowWhitespaces(false);

        TextEditorCoordinates coordinates = textEditorPos.get(location);
        if (coordinates != null) textEditor.setCursorPosition(coordinates);

        float v = textEditor.getTotalLines() * (ImGui.getFont().getFontSize()) + 16;
        textEditor.render("Shader Editor - " + location, ImGui.getContentRegionAvailX(), v);

        if (textEditor.isCursorPositionChanged()) {
            textEditorPos.put(location, textEditor.getCursorPosition());
        }


        if (ImGui.isItemHovered()) {
            ImGui.setTooltip("Click to copy to clipboard");
            if (ImGui.isItemClicked()) {
                ImGui.setClipboardText(source);
            }
        }
    }

    private static void showShaderNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Shader Node", showShaderNode)) {
            ImGui.text("Shader Node");
            ImGui.text("Shader Classname: " + shader.getClass().getName());
            ImGui.text("Shader ID: " + shader.hashCode());
            ImGui.text("Shader Program ID: " + shader.hashCode());

            if (shader instanceof BaseShader) {
                BaseShader baseShader = (BaseShader) shader;
                showSource(baseShader.program, baseShader.program.getVertexShaderSource(), "vertex");
                showSource(baseShader.program, baseShader.program.getFragmentShaderSource(), "fragment");
            }
        }
        ImGui.end();
    }

    private static void showShaderProgramListWindow() {
        if (ImGui.begin("Shader Program List")) {
            ImGui.text("Shader Program List");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
            if (ImGui.beginListBox("##ShaderPrograms{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                for (ShaderProgram shaderProgram : GdxRegistries.SHADER_PROGRAMS) {
                    if (ImGui.selectable("Shader Program ID: " + shaderProgram.hashCode(), ImGuiHandler.shaderProgram == shaderProgram)) {
                        showShaderProgramNode.set(true);
                        ImGuiHandler.shaderProgram = shaderProgram;
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Shader Program ID: " + shaderProgram.getHandle());
                        ImGui.endTooltip();
                    }
                }
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showShaderProgramNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Shader Program Node", showShaderProgramNode)) {
            ImGui.text("Shader Program Node");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");

            if (shaderProgram != null) {
                showSource(shaderProgram, shaderProgram.getVertexShaderSource(), "vertex");
                ImGui.separator();
                showSource(shaderProgram, shaderProgram.getFragmentShaderSource(), "fragment");
            } else {
                ImGui.text("Shader Program has been disposed");
                if (ImGui.button("Close")) {
                    showShaderProgramNode.set(false);
                }
            }
        }
        ImGui.end();
    }

    private static void showModelListWindow() {
        if (ImGui.begin("Model List")) {
            ImGui.text("Model List");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
            if (ImGui.beginListBox("##Models{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showModelNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Model Node", showModelNode)) {
            ImGui.text("Model Node");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
        }
        ImGui.end();
    }

    private static void showTextureAtlasListWindow() {
        if (ImGui.begin("Texture Atlas List", showTextureAtlasList)) {
            ImGui.text("Texture Atlas List");
            if (ImGui.beginListBox("##TextureAtlas{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                for (TextureAtlas textureAtlas : GdxRegistries.TEXTURE_ATLASES) {
                    if (ImGui.selectable("Texture Atlas (" + textureAtlas.getRegions().size + "x)", ImGuiHandler.textureAtlas == textureAtlas)) {
                        showTextureAtlasNode.set(true);
                        ImGuiHandler.textureAtlas = textureAtlas;
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Textures: " + textureAtlas.getTextures().size);
                        ImGui.text("Regions: " + textureAtlas.getRegions().size);
                        ImGui.endTooltip();
                    }
                }
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showTextureAtlasNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Texture Atlas Node", showTextureAtlasNode)) {
            try {
                if (textureAtlas != null) {
                    if (textureAtlas.getTextures().size == 0) {
                        textureAtlas = null;
                    }

                    if (ImGui.beginListBox("##TextureAtlas{}::MixinProvider::Pages", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                        try {
                            Array<TextureAtlas.AtlasRegion> textures = textureAtlas.getRegions();
                            for (int i = 0; i < textures.size; i++) {
                                TextureAtlas.AtlasRegion region = textures.get(i);
                                if (ImGui.selectable("Texture ID: " + region.getTexture().getTextureObjectHandle() + " (" + region.getRegionWidth() + "x" + region.getRegionHeight() + ")",
                                        ImGuiHandler.texture != null
                                                && Objects.equals(ImGuiHandler.texture.getTexture(), region.getTexture())
                                                && ImGuiHandler.texture.getRegionX() == region.getRegionX()
                                                && ImGuiHandler.texture.getRegionY() == region.getRegionY()
                                                && ImGuiHandler.texture.getRegionWidth() == region.getRegionWidth()
                                                && ImGuiHandler.texture.getRegionHeight() == region.getRegionHeight()
                                )) {
                                    showTextureNode.set(true);
                                    ImGuiHandler.texture = region;
                                }
                                if (ImGui.isItemHovered()) {
                                    ImGui.beginTooltip();
                                    ImGui.text("Texture: " + region.getTexture().getTextureObjectHandle() + " (" + region.getRegionWidth() + "x" + region.getRegionHeight() + "+" + region.getRegionX() + "x" + region.getRegionY() + ")");
                                    resizer.set(region.getRegionWidth(), region.getRegionHeight());
                                    Vec2f fit = resizer.fit(256, 128);
                                    ImGui.image(region.getTexture().getTextureObjectHandle(), fit.x, fit.y, region.getU(), region.getV(), region.getU2(), region.getV2());
                                    ImGui.endTooltip();
                                }
                            }
                        } finally {
                            ImGui.endListBox();
                        }
                    }
                }
            } catch (Exception e) {
                textureAtlas = null;
            }

            if (textureAtlas == null) {
                ImGui.text("Texture Atlas has been disposed");
                if (ImGui.button("Close")) {
                    showTextureAtlasNode.set(false);
                }
            }
        }
        ImGui.end();
    }

    private static void showTextureNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Texture Node", showTextureNode)) {
            try {
                if (texture != null) {
                    ImGui.text("Texture: " + texture.getTexture().getTextureObjectHandle() + " (" + texture.getRegionWidth() + "x" + texture.getRegionHeight() + ")");
                    resizer.set(texture.getRegionWidth(), texture.getRegionHeight());
                    Vec2f fit = resizer.fit(ImGui.getContentRegionAvail().x, ImGui.getContentRegionAvail().y);
                    ImGui.image(texture.getTexture().getTextureObjectHandle(), fit.x, fit.y, texture.getU(), texture.getV(), texture.getU2(), texture.getV2());
                }
            } catch (Exception e) {
                texture = null;
            }

            if (texture == null) {
                ImGui.text("Texture has been disposed");
                if (ImGui.button("Close")) {
                    showTextureNode.set(false);
                }
            }
        }
        ImGui.end();
    }

    private static void showTextureListWindow() {
        if (ImGui.begin("Texture List")) {
            ImGui.text("Texture List");
            if (ImGui.beginListBox("##Textures{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                for (Texture texture : GdxRegistries.TEXTURES) {
                    if (ImGui.selectable("Texture ID: " + texture.getTextureObjectHandle() + " (" + texture.getWidth() + "x" + texture.getHeight() + ")",
                            ImGuiHandler.texture != null
                                    && Objects.equals(ImGuiHandler.texture.getTexture(), texture)
                                    && ImGuiHandler.texture.getRegionX() == 0
                                    && ImGuiHandler.texture.getRegionY() == 0
                                    && ImGuiHandler.texture.getRegionWidth() == texture.getWidth()
                                    && ImGuiHandler.texture.getRegionHeight() == texture.getHeight()
                    )) {
                        showTextureNode.set(true);
                        ImGuiHandler.texture = new TextureRegion(texture);
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Texture: " + texture.getTextureObjectHandle());
                        resizer.set(texture.getWidth(), texture.getHeight());
                        Vec2f fit = resizer.fit(256, 128);
                        ImGui.image(texture.getTextureObjectHandle(), fit.x, fit.y);
                        ImGui.endTooltip();
                    }
                }
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    public static boolean isPaused() {
        return PAUSED.get();
    }
}
