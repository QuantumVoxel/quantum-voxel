//package dev.ultreon.quantum.client.input.controller.gui;
//
//import dev.ultreon.quantum.client.gui.Bounds;
//import dev.ultreon.quantum.client.gui.Position;
//import dev.ultreon.quantum.client.gui.Renderer;
//import dev.ultreon.quantum.client.gui.widget.Button;
//import org.checkerframework.common.value.qual.IntRange;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.function.Supplier;
//
//public class Keycap extends Button<Keycap> {
//    private final KeyMappingIcon icon;
//
//    protected Keycap(KeyMappingIcon icon, int width, int height) {
//        super(width, height);
//        this.icon = icon;
//    }
//
//    @Override
//    public void render(@NotNull Renderer renderer, float deltaTime) {
//        super.render(renderer, deltaTime);
//
//        this.icon.render(renderer, this.pos.x, this.pos.y, this.isFocused);
//    }
//
//    @Override
//    public Keycap position(Supplier<Position> position) {
//        this.onRevalidate(widget -> widget.setPos(position.get()));
//        return this;
//    }
//
//    @Override
//    public Keycap bounds(Supplier<Bounds> position) {
//        this.onRevalidate(widget -> widget.setBounds(position.get()));
//        return this;
//    }
//}
