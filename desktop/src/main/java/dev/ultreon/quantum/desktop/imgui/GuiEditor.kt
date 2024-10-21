package dev.ultreon.quantum.desktop.imgui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import dev.ultreon.quantum.client.QuantumClient
import dev.ultreon.quantum.client.gui.Screen
import dev.ultreon.quantum.client.gui.widget.UIContainer
import dev.ultreon.quantum.client.gui.widget.Widget
import dev.ultreon.quantum.desktop.imgui.ImGuiEx.button
import dev.ultreon.quantum.desktop.imgui.ImGuiEx.editBool
import dev.ultreon.quantum.desktop.imgui.ImGuiEx.editInt
import dev.ultreon.quantum.desktop.imgui.ImGuiEx.editString
import dev.ultreon.quantum.desktop.imgui.ImGuiEx.text
import imgui.ImGui
import java.util.function.Supplier

class GuiEditor {
  fun render(client: QuantumClient) {
    val currentScreen = client.screen

    text("Classname:", Supplier { currentScreen!!.javaClass.simpleName })
    if (currentScreen != null) {
      val widgets =
        currentScreen.getWidgetsAt((Gdx.input.x / client.guiScale).toInt(), (Gdx.input.y / client.guiScale).toInt())
      for (widget in widgets) {
        if (widget != null) {
          client.shapes.batch.begin()
          if (widget is UIContainer<*>) {
            client.shapes.setColor(Color.CYAN)
          } else {
            client.shapes.setColor(Color.MAGENTA)
          }
          client.shapes.rectangle(
            widget.x * client.guiScale, widget.y * client.guiScale - 1,
            widget.width * client.guiScale + 1, widget.height * client.guiScale + 1
          )
          client.shapes.batch.end()
        }
      }
      text("Widget:",
        Supplier {
          widgets.stream().findFirst().map { widget: Widget -> widget.path().fileName }
            .orElse(null)
        })
    }

    if (currentScreen != null) {
      renderTools(currentScreen)
    }
  }

  companion object {
    private fun renderTools(screen: Screen) {
      editBool("Enabled", "::enabled",
        { screen.isEnabled() },
        { enabled: Boolean? ->
          screen.setEnabled(
            enabled!!
          )
        })
      editBool("Visible", "::visible",
        { screen.isVisible() },
        { visible: Boolean? ->
          screen.setVisible(
            visible!!
          )
        })
      editString("Title", "::title",
        { screen.rawTitle },
        { title: String? -> screen.title(title!!) })
      button("Back", "::back") { screen.back() }

      if (ImGui.collapsingHeader("Widgets")) {
        ImGui.treePush()

        val children = screen.children()
        var i = 0
        val childrenSize = children.size
        while (i < childrenSize) {
          val component = children[i]
          if (component == null) {
            i++
            continue
          }

          renderWidgetTools(i, component)
          i++
        }

        ImGui.treePop()
      }
    }

    private fun renderWidgetTools(index: Int, widget: Widget) {
      if (ImGui.collapsingHeader("Widget #" + index + ": " + widget.path().fileName)) {
        ImGui.treePush()
        val path = widget.path().toString()

        text("Package: ") { widget.javaClass.packageName }
        text("Classname: ") { widget.javaClass.simpleName }
        editBool("Enabled: ", "$path::enabled",
          { widget.isEnabled() },
          { enabled: Boolean? ->
            widget.setEnabled(
              enabled!!
            )
          })
        editBool("Visible: ", "$path::visible",
          { widget.isVisible() },
          { visible: Boolean? ->
            widget.setVisible(
              visible!!
            )
          })

        // Properties
        val components = widget.componentRegistry()
        for ((key, value) in components) {
          value.handleImGui("$path::$key", key, widget)
        }

        if (ImGui.collapsingHeader("Position")) {
          ImGui.treePush()
          editInt("X: ", "$path::pos::x",
            { widget.x },
            { x: Int? -> widget.x = x!! })
          editInt("Y: ", "$path::pos::y",
            { widget.y },
            { y: Int? -> widget.y = y!! })
          ImGui.treePop()
        }
        if (ImGui.collapsingHeader("Size")) {
          ImGui.treePush()
          editInt("Width: ", "$path::size::width",
            { widget.width },
            { width: Int? -> widget.width(width!!) })
          editInt("Height: ", "$path::size::height",
            { widget.height },
            { height: Int? -> widget.height(height!!) })
          ImGui.treePop()
        }

        if (widget is UIContainer<*> && ImGui.collapsingHeader("Children")) {
          ImGui.treePush()
          val children = widget.children()
          var i = 0
          val childrenSize = children.size
          while (i < childrenSize) {
            val child = children[i]
            if (child == null) {
              i++
              continue
            }

            renderWidgetTools(i, child)
            i++
          }
          ImGui.treePop()
        }

        ImGui.treePop()
      }
    }
  }
}
