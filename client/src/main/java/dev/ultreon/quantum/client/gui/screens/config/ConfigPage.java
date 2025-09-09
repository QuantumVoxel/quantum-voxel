package dev.ultreon.quantum.client.gui.screens.config;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.screens.config.entries.*;
import dev.ultreon.quantum.client.gui.screens.tabs.Tab;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.config.api.props.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigPage extends Tab {
    private static final Color OVERLAY = new Color(1, 1, 1, 0.2f);
    private final SelectionList<ConfigProperty<?>> list;
    private final List<Widget> widgetLists = new ArrayList<>();
    private final List<String> nameLists = new ArrayList<>();
    private final List<ConfigProperty<?>> propertyLists = new ArrayList<>();
    private int lastWidth;
    private int lastHeight;

    public ConfigPage(String title) {
        super(title);

        list = add(new SelectionList<>());
        list.setItemRenderer(this::renderItem);
        list.setDrawBackground(false);
        list.withCutButtons(false);
        list.withDrawButtons(false);
        list.setSelectable(false);
        list.setItemHeight(30);
        list.setPos(pos.x, pos.y);
        list.setSize(size.width, size.height);
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        return list.mouseWheel(mouseX, mouseY, rotation);
    }

    private void renderItem(Renderer renderer, ConfigProperty<?> configProperty, int y, boolean b, float v) {
        int i = propertyLists.indexOf(configProperty);
        if (mousePos.y >= y && mousePos.y < y + list.getItemHeight()) {
            renderer.fill(list.pos.x, y, list.size.width, list.getItemHeight(), OVERLAY);
        }

        renderer.textLeft(nameLists.get(i), list.pos.x + 10, y + list.getItemHeight() / 2, Color.WHITE);

        Widget widget = widgetLists.get(i);
        widget.setY(y);
        widget.setPos(list.pos.x + list.size.width - 210, widget.getY());
        widget.setSize(200, list.getItemHeight());
        widget.renderWidget(renderer, v);
    }

    @Override
    public void render(@NotNull Renderer renderer, float deltaTime) {
        if (lastWidth != size.width || lastHeight != size.height) {
            list.setPos(pos.x, pos.y);
            list.setSize(size.width, size.height);
            lastWidth = size.width;
            lastHeight = size.height;
        }

        super.render(renderer, deltaTime);
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        renderer.drawPlatform(pos.x - 2, pos.y, this.size.width + 4, this.size.height);

        super.renderWidget(renderer, deltaTime);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        for (Widget widget : widgetLists) {
            if (widget.isWithinBounds(mouseX, mouseY)) {
                widget.mousePress(mouseX, mouseY, button);
                return true;
            }
        }

        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        for (Widget widget : widgetLists) {
            widget.mouseRelease(mouseX, mouseY, button);
        }

        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public void renderChildren(@NotNull Renderer renderer, float deltaTime) {
        list.render(renderer, deltaTime);
    }

    public void addEntry(String name, ConfigProperty<?> property) {
        Widget propertyWidget = createWidgetFor(property);
        propertyLists.add(property);
        widgetLists.add(propertyWidget);
        nameLists.add(name);
        list.addEntry(property);

        if (propertyWidget != null) {
            add(propertyWidget);
        }
    }

    private Widget createWidgetFor(ConfigProperty<?> property) {
        if (property instanceof ByteProperty) {
            return new ByteEntry((ByteProperty) property);
        } else if (property instanceof ShortProperty) {
            return new ShortEntry((ShortProperty) property);
        } else if (property instanceof IntProperty) {
            return new IntEntry((IntProperty) property);
        } else if (property instanceof LongProperty) {
            return new LongEntry((LongProperty) property);
        } else if (property instanceof FloatProperty) {
            return new FloatEntry((FloatProperty) property);
        } else if (property instanceof DoubleProperty) {
            return new DoubleEntry((DoubleProperty) property);
        } else if (property instanceof BooleanProperty) {
            return new BooleanEntry((BooleanProperty) property);
        } else if (property instanceof StringProperty) {
            return new StringEntry((StringProperty) property);
        } else if (property instanceof NamespaceIDProperty) {
            return new NamespaceIDEntry((NamespaceIDProperty) property);
        } else {
            CommonConstants.LOGGER.error("Unknown property type: " + property.getClass().getName());
            return null;
        }
    }
}
