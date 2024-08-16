package dev.ultreon.quantum.desktop.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.UIContainer;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.gui.widget.components.UIComponent;
import dev.ultreon.quantum.util.NamespaceID;
import imgui.ImGui;

import java.util.List;
import java.util.Map;

public class GuiEditor {
    public void render(QuantumClient client) {
        var currentScreen = client.screen;

        ImGuiEx.text("Classname:", () -> currentScreen == null ? null : currentScreen.getClass().getSimpleName());
        if (currentScreen != null) {
            var widgets = currentScreen.getWidgetsAt((int) (Gdx.input.getX() / client.getGuiScale()), (int) (Gdx.input.getY() / client.getGuiScale()));
            for (var widget : widgets) {
                if (widget != null) {
                    client.shapes.getBatch().begin();
                    if (widget instanceof UIContainer<?>) {
                        client.shapes.setColor(Color.CYAN);
                    } else {
                        client.shapes.setColor(Color.MAGENTA);
                    }
                    client.shapes.rectangle(
                            widget.getX() * client.getGuiScale(), widget.getY() * client.getGuiScale() - 1,
                            widget.getWidth() * client.getGuiScale() + 1, widget.getHeight() * client.getGuiScale() + 1);
                    client.shapes.getBatch().end();
                }
            }
            ImGuiEx.text("Widget:", () -> widgets.stream().findFirst().map(widget -> widget.path().getFileName()).orElse(null));
        }

        if (currentScreen != null) {
            GuiEditor.renderTools(currentScreen);
        }
    }

    private static void renderTools(Screen screen) {
        ImGuiEx.editBool("Enabled", "::enabled", screen::isEnabled, screen::setEnabled);
        ImGuiEx.editBool("Visible", "::visible", screen::isVisible, screen::setVisible);
        ImGuiEx.editString("Title", "::title", screen::getRawTitle, screen::title);
        ImGuiEx.button("Back", "::back", screen::back);

        if (ImGui.collapsingHeader("Widgets")) {
            ImGui.treePush();

            var children = screen.children();
            for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
                var component = children.get(i);
                if (component == null) continue;

                GuiEditor.renderWidgetTools(i, component);
            }

            ImGui.treePop();
        }
    }

    private static void renderWidgetTools(int index, Widget widget) {
        if (ImGui.collapsingHeader("Widget #" + index + ": " + widget.path().getFileName())) {
            ImGui.treePush();
            var path = widget.path().toString();

            ImGuiEx.text("Package: ", () -> widget.getClass().getPackageName());
            ImGuiEx.text("Classname: ", () -> widget.getClass().getSimpleName());
            ImGuiEx.editBool("Enabled: ", path + "::enabled", widget::isEnabled, widget::setEnabled);
            ImGuiEx.editBool("Visible: ", path + "::visible", widget::isVisible, widget::setVisible);

            // Properties
            var components = widget.componentRegistry();
            for (Map.Entry<NamespaceID, UIComponent> component : components.entrySet()) {
                component.getValue().handleImGui(path + "::" + component.getKey(), component.getKey(), widget);
            }

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editInt("X: ", path + "::pos::x", widget::getX, widget::x);
                ImGuiEx.editInt("Y: ", path + "::pos::y", widget::getY, widget::y);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Size")) {
                ImGui.treePush();
                ImGuiEx.editInt("Width: ", path + "::size::width", widget::getWidth, widget::width);
                ImGuiEx.editInt("Height: ", path + "::size::height", widget::getHeight, widget::height);
                ImGui.treePop();
            }

            if (widget instanceof UIContainer<?> container && ImGui.collapsingHeader("Children")) {
                ImGui.treePush();
                List<? extends Widget> children = container.children();
                for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
                    var child = children.get(i);
                    if (child == null) continue;

                    GuiEditor.renderWidgetTools(i, child);
                }
                ImGui.treePop();
            }

            ImGui.treePop();
        }
    }
}
