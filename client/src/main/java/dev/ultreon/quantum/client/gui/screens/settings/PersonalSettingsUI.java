package dev.ultreon.quantum.client.gui.screens.settings;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.screens.options.BooleanEnum;
import dev.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.Slider;
import dev.ultreon.quantum.text.TextObject;

public class PersonalSettingsUI {
    static final TextObject TITLE = TextObject.translation("quantum.screen.options.personalisation.title");
    private QuantumClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();
        
        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.diagonalFontShadow"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.diagonalFontShadow ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setDiagonalFontShadow));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.enforceUnicode"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.enforceUnicode ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setEnforceUnicode));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.blurRadius"), new Slider(ClientConfig.blurRadius == 0 ? 32 : (int) ClientConfig.blurRadius, 4, 128)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .text(TextObject.translation("quantum.screen.options.personalisation.blurRadius.text"))
                .callback(this::setBlurRadius));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.blurEnabled"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.blurEnabled ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setBlurEnabled));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.vignetteOpacity"), new Slider((int) (ClientConfig.vignetteOpacity * 100), 0, 100)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .text(TextObject.translation("quantum.screen.options.personalisation.vignetteOpacity.text"))
                .callback(this::setVignetteOpacity));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.showMemoryUsage"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.showMemoryUsage ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 175, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setShowMemoryUsage));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.enableFpsCounter"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.enableFpsCounter ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 200, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setFpsCounter));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.showOnlyCraftable"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.showOnlyCraftable ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 225, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setShowOnlyCraftable));

        CycleButton<BooleanEnum> fullVibrancy = builder.add(TextObject.translation("quantum.screen.options.personalisation.fullVibrancy"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(GamePlatform.get().getFullVibrancy() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 250, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setFullVibrancy));

        CycleButton<BooleanEnum> windowVibrancy = builder.add(TextObject.translation("quantum.screen.options.personalisation.windowVibrancy"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(GamePlatform.get().getWindowVibrancy() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 275, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .callback(this::setWindowVibrancy));

        if (!GamePlatform.get().isVibrancySupported()) {
            fullVibrancy.disable();
            windowVibrancy.disable();
        }
    }

    private void setFullVibrancy(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        GamePlatform.get().setFullVibrancy(booleanEnumCycleButton.getValue().get());
    }

    private void setWindowVibrancy(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        GamePlatform.get().setWindowVibrancy(booleanEnumCycleButton.getValue().get());
    }

    private void setShowOnlyCraftable(CycleButton<BooleanEnum> cycleButton) {
        ClientConfig.showOnlyCraftable = cycleButton.getValue().get();
        this.client.newConfig.save();
    }

    private void setFpsCounter(CycleButton<BooleanEnum> cycleButton) {
        ClientConfig.enableFpsCounter = cycleButton.getValue().get();
        this.client.newConfig.save();
    }

    private void setShowMemoryUsage(CycleButton<BooleanEnum> cycleButton) {
        ClientConfig.showMemoryUsage = cycleButton.getValue().get();
        this.client.newConfig.save();
    }

    private void setBlurEnabled(CycleButton<BooleanEnum> cycleButton) {
        ClientConfig.blurEnabled = cycleButton.getValue().get();
        this.client.newConfig.save();
    }

    private void setVignetteOpacity(Slider slider) {
        ClientConfig.vignetteOpacity = slider.value().get() / 100f;
        this.client.newConfig.save();
    }

    private void setBlurRadius(Slider slider) {
        ClientConfig.blurRadius = (float) slider.value().get();
        this.client.newConfig.save();
    }

    private void setEnforceUnicode(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        ClientConfig.enforceUnicode = booleanEnumCycleButton.getValue().get();
        this.client.newConfig.save();
    }

    private void setDiagonalFontShadow(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        ClientConfig.diagonalFontShadow = booleanEnumCycleButton.getValue().get();
        this.client.newConfig.save();
    }
}
