package dev.ultreon.quantum.client.gui.screens.settings;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.config.ClientConfiguration;
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

    public void build(TabBuilder builder) {

        builder.add(Label.of(builder.title())
                .withAlignment(Alignment.CENTER)
                .withScale(2)
                .withPositioning(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.diagonalFontShadow"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.diagonalFontShadow.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .withCallback(this::setDiagonalFontShadow));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.enforceUnicode"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.enforceUnicode.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .withCallback(this::setEnforceUnicode));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.blurRadius"), new Slider(ClientConfiguration.blurRadius.getValue() == 0 ? 32 : ClientConfiguration.blurRadius.getValue().intValue(), 4, 128)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .text(TextObject.translation("quantum.screen.options.personalisation.blurRadius.text"))
                .setCallback(this::setBlurRadius));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.blurEnabled"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.blurEnabled.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .withCallback(this::setBlurEnabled));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.showMemoryUsage"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.showMemoryUsage.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 175, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .withCallback(this::setShowMemoryUsage));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.enableFpsCounter"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.enableFpsHud.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 200, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .withCallback(this::setFpsCounter));

        builder.add(TextObject.translation("quantum.screen.options.personalisation.showOnlyCraftable"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.showOnlyCraftable.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 225, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .withCallback(this::setShowOnlyCraftable));

        CycleButton<BooleanEnum> fullVibrancy = builder.add(TextObject.translation("quantum.screen.options.personalisation.fullVibrancy"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(GamePlatform.get().getFullVibrancy() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 250, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .withCallback(this::setFullVibrancy));

        CycleButton<BooleanEnum> windowVibrancy = builder.add(TextObject.translation("quantum.screen.options.personalisation.windowVibrancy"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(GamePlatform.get().getWindowVibrancy() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 275, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.enabled" : "quantum.ui.disabled"))
                .withCallback(this::setWindowVibrancy));

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
        ClientConfiguration.showOnlyCraftable.setValue(cycleButton.getValue().get());
        ClientConfiguration.save();
    }

    private void setFpsCounter(CycleButton<BooleanEnum> cycleButton) {
        ClientConfiguration.enableFpsHud.setValue(cycleButton.getValue().get());
        ClientConfiguration.save();
    }

    private void setShowMemoryUsage(CycleButton<BooleanEnum> cycleButton) {
        ClientConfiguration.showMemoryUsage.setValue(cycleButton.getValue().get());
        ClientConfiguration.save();
    }

    private void setBlurEnabled(CycleButton<BooleanEnum> cycleButton) {
        ClientConfiguration.blurEnabled.setValue(cycleButton.getValue().get());
        ClientConfiguration.save();
    }

    private void setBlurRadius(Slider slider) {
        ClientConfiguration.blurRadius.setValue((float) slider.value().get());
        ClientConfiguration.save();
    }

    private void setEnforceUnicode(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        ClientConfiguration.enforceUnicode.setValue(booleanEnumCycleButton.getValue().get());
        ClientConfiguration.save();
    }

    private void setDiagonalFontShadow(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        ClientConfiguration.diagonalFontShadow.setValue(booleanEnumCycleButton.getValue().get());
        ClientConfiguration.save();
    }
}
