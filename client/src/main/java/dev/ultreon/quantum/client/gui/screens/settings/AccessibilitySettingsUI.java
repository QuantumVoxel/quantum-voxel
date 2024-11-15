package dev.ultreon.quantum.client.gui.screens.settings;

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

public class AccessibilitySettingsUI {
    static final TextObject TITLE = TextObject.translation("quantum.screen.options.accessibility.title");
    private QuantumClient client;

    public AccessibilitySettingsUI() {
        super();
    }

    public void build(TabBuilder builder) {
        this.client = builder.client();
        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.hideFirstPersonPlayer"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.hideFirstPersonPlayer ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.hidden") : TextObject.translation("quantum.ui.visible"))
                .callback(this::setHideFirstPersonPlayer));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.hideHotbarWhenThirdPerson"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.hideHotbarWhenThirdPerson ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.hidden" : "quantum.ui.visible"))
                .callback(this::setHideHotbarWhenThirdPerson));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.vibration"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.vibration ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setVibration));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.enableCrosshair"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.enableCrosshair ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setEnableCrosshair));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.enableHud"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.enableHud ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setEnableHud));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.doublePressDelay"), new Slider(ClientConfig.doublePressDelay, 1, 3)
                .value(ClientConfig.doublePressDelay / 20)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 175, 150, 21))
                .callback(this::setDoublePressDelay));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.doubleTapDelay"), new Slider(ClientConfig.doubleTapDelay, 1, 3)
                .value(ClientConfig.doubleTapDelay / 20)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 200, 150, 21))
                .callback(this::setDoubleTapDelay));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.exitConfirmation"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.showClosePrompt ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 225, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .callback(this::setExitConfirmation));
    }

    private void setExitConfirmation(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        ClientConfig.showClosePrompt = booleanEnumCycleButton.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }

    private void setDoubleTapDelay(Slider slider) {
        ClientConfig.doubleTapDelay = slider.value().get() * 20;
        this.client.newConfig.save();
    }

    private void setDoublePressDelay(Slider slider) {
        ClientConfig.doublePressDelay = slider.value().get() * 20;
        this.client.newConfig.save();
    }

    private void setEnableHud(CycleButton<BooleanEnum> cycleButton) {
        ClientConfig.enableHud = cycleButton.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }

    private void setEnableCrosshair(CycleButton<BooleanEnum> cycleButton) {
        ClientConfig.enableCrosshair = cycleButton.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }

    private void setHideFirstPersonPlayer(CycleButton<BooleanEnum> value) {
        ClientConfig.hideFirstPersonPlayer = value.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }

    private void setHideHotbarWhenThirdPerson(CycleButton<BooleanEnum> value) {
        ClientConfig.hideHotbarWhenThirdPerson = value.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }

    private void setVibration(CycleButton<BooleanEnum> value) {
        ClientConfig.vibration = value.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }
}
