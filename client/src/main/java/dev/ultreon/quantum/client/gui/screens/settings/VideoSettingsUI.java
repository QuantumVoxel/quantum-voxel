package dev.ultreon.quantum.client.gui.screens.settings;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.screens.options.BooleanEnum;
import dev.ultreon.quantum.client.gui.screens.options.Scale;
import dev.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.text.TextObject;

import java.util.Objects;

import static dev.ultreon.quantum.world.World.CS;

public class VideoSettingsUI {
    static final TextObject TITLE = TextObject.translation("quantum.screen.options.video.title");
    private QuantumClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();

        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));

        builder.add(TextObject.translation("quantum.screen.options.video.fov"), Slider.of(30, 160)
                .value(ClientConfig.fov)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .setCallback(this::setFov));

        builder.add(TextObject.translation("quantum.screen.options.video.renderDistance"), Slider.of(CS, 256)
                .value(ClientConfig.renderDistance)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .setCallback(this::setRenderDistance));

        builder.add(TextObject.translation("quantum.screen.options.video.vSync"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.enableVsync ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .setCallback(this::setVsync));

        builder.add(TextObject.translation("quantum.screen.options.video.fullscreen"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.fullscreen ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .setCallback(this::setFullscreen));

        builder.add(TextObject.translation("quantum.screen.options.video.guiScale"), new CycleButton<Scale>()
                .values(Scale.values())
                .value(Objects.requireNonNullElse(Scale.of(ClientConfig.guiScale), Scale.MEDIUM))
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .formatter(scale -> {
                    if (scale.get() == 0) {
                        return TextObject.translation("quantum.ui.auto");
                    }
                    return TextObject.literal(scale.get() + "x");
                })
                .setCallback(this::setScale));

        builder.add(TextObject.translation("quantum.screen.options.video.frameRate"), Slider.of(10, 240)
                .value(ClientConfig.fpsLimit)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 175, 150, 21))
                .setCallback(this::setFrameRate));
    }

    private void setFpsCounter(CycleButton<BooleanEnum> cycleButton) {
        ClientConfig.enableFpsCounter = cycleButton.getValue().get();
        this.client.newConfig.save();
    }

    private void setFrameRate(Slider slider) {
        ClientConfig.fpsLimit = slider.value().get();
        this.client.newConfig.save();
    }

    private void setVsync(CycleButton<BooleanEnum> button) {
        ClientConfig.enableVsync = button.getValue().get();
        this.client.newConfig.save();
    }

    private void setScale(CycleButton<Scale> caller) {
        int value = caller.getValue().get();
        this.client.setAutomaticScale(caller.getValue() == Scale.AUTO);
        this.client.setGuiScale(value == 0 ? this.client.calcMaxGuiScale() : value);
        ClientConfig.guiScale = value;
        this.client.newConfig.save();
    }

    private void setFullscreen(CycleButton<BooleanEnum> caller) {
        boolean bool = caller.getValue().get();
        ClientConfig.fullscreen = bool;
        this.client.setFullScreen(bool);
        this.client.newConfig.save();
    }

    private void setFov(Slider slider) {
        int fov = slider.value().get();
        ClientConfig.fov = fov;
        this.client.camera.fov = fov;
        this.client.newConfig.save();
    }

    private void setRenderDistance(Slider slider) {
        ClientConfig.renderDistance = slider.value().get();
        this.client.newConfig.save();
    }
}
