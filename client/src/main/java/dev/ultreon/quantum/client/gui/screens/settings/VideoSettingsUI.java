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

public class VideoSettingsUI {
    static final TextObject TITLE = TextObject.translation("quantum.screen.options.video.title");
    private QuantumClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();

        builder.add(TextObject.translation("quantum.screen.options.video.fov"), Slider.of(200, 30, 160)
                .value(ClientConfig.fov)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 10, 150, 21))
                .callback(this::setFov));

        builder.add(TextObject.translation("quantum.screen.options.video.renderDistance"), Slider.of(200, 2, 16)
                .value(ClientConfig.renderDistance)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 35, 150, 21))
                .callback(this::setRenderDistance));

        builder.add(TextObject.translation("quantum.screen.options.video.vSync"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.enableVsync ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 60, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setVsync));

        builder.add(TextObject.translation("quantum.screen.options.video.fullscreen"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.fullscreen ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 85, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setFullscreen));

        builder.add(TextObject.translation("quantum.screen.options.video.guiScale"), new CycleButton<Scale>()
                .values(Scale.values())
                .value(Objects.requireNonNullElse(Scale.of(ClientConfig.guiScale), Scale.MEDIUM))
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 110, 150, 21))
                .formatter(scale -> {
                    if (scale.get() == 0) {
                        return TextObject.translation("quantum.ui.auto");
                    }
                    return TextObject.literal(scale.get() + "setX");
                })
                .callback(this::setScale));

        builder.add(TextObject.translation("quantum.screen.options.video.frameRate"), Slider.of(200, 10, 240)
                .value(ClientConfig.fpsLimit)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 135, 150, 21))
                .callback(this::setFrameRate));
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
        this.client.setGuiScale(value);
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
