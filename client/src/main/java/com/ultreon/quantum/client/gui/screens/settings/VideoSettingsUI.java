package com.ultreon.quantum.client.gui.screens.settings;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.config.Config;
import com.ultreon.quantum.client.gui.Alignment;
import com.ultreon.quantum.client.gui.Bounds;
import com.ultreon.quantum.client.gui.Position;
import com.ultreon.quantum.client.gui.screens.options.BooleanEnum;
import com.ultreon.quantum.client.gui.screens.options.Scale;
import com.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import com.ultreon.quantum.client.gui.widget.CycleButton;
import com.ultreon.quantum.client.gui.widget.Label;
import com.ultreon.quantum.client.gui.widget.Slider;
import com.ultreon.quantum.text.TextObject;

import java.util.Objects;

public class VideoSettingsUI {
    static final TextObject TITLE = TextObject.translation("quantum.screen.options.video.title");
    private QuantumClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();

        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));


        builder.add(TextObject.translation("quantum.screen.options.video.fov"), Slider.of(200, 30, 160)
                .value(Config.fov)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .callback(this::setFov));

        builder.add(TextObject.translation("quantum.screen.options.video.renderDistance"), Slider.of(200, 2, 16)
                .value(Config.renderDistance)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .callback(this::setRenderDistance));

        builder.add(TextObject.translation("quantum.screen.options.video.vSync"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.enableVsync ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setVsync));

        builder.add(TextObject.translation("quantum.screen.options.video.fullscreen"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.fullscreen ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setFullscreen));

        builder.add(TextObject.translation("quantum.screen.options.video.guiScale"), new CycleButton<Scale>()
                .values(Scale.values())
                .value(Objects.requireNonNullElse(Scale.of(Config.guiScale), Scale.MEDIUM))
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .formatter(scale -> {
                    if (scale.get() == 0) {
                        return TextObject.literal("Automatic");
                    }
                    return TextObject.literal(scale.get() + "x");
                })
                .callback(this::setScale));

        builder.add(TextObject.translation("quantum.screen.options.video.frameRate"), Slider.of(200, 10, 240)
                .value(Config.fpsLimit)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 175, 150, 21))
                .callback(this::setFrameRate));
    }

    private void setFrameRate(Slider slider) {
        Config.fpsLimit = slider.value().get();
        this.client.newConfig.save();
    }

    private void setVsync(CycleButton<BooleanEnum> button) {
        Config.enableVsync = button.getValue().get();
        this.client.newConfig.save();
    }

    private void setScale(CycleButton<Scale> caller) {
        int value = caller.getValue().get();
        this.client.setAutomaticScale(caller.getValue() == Scale.AUTO);
        this.client.setGuiScale(value);
        Config.guiScale = value;
        this.client.newConfig.save();
    }

    private void setFullscreen(CycleButton<BooleanEnum> caller) {
        boolean bool = caller.getValue().get();
        Config.fullscreen = bool;
        this.client.setFullScreen(bool);
        this.client.newConfig.save();
    }

    private void setFov(Slider slider) {
        int fov = slider.value().get();
        Config.fov = fov;
        this.client.camera.fov = fov;
        this.client.newConfig.save();
    }

    private void setRenderDistance(Slider slider) {
        Config.renderDistance = slider.value().get();
        this.client.newConfig.save();
    }
}
