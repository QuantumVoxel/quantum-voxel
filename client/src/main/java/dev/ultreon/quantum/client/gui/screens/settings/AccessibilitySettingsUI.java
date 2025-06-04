package dev.ultreon.quantum.client.gui.screens.settings;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Alignment;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.screens.options.BooleanEnum;
import dev.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import dev.ultreon.quantum.client.gui.widget.CycleButton;
import dev.ultreon.quantum.client.gui.widget.Label;
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
                .withPositioning(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.hideHotbarWhenThirdPerson"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.thirdpersonHotbar.getValue() ? BooleanEnum.FALSE : BooleanEnum.TRUE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.hidden" : "quantum.ui.visible"))
                .withCallback(this::setHideHotbarWhenThirdPerson));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.vibration"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.vibration.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .withCallback(this::setVibration));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.enableCrosshair"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.enableCrosshair.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .withCallback(this::setEnableCrosshair));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.enableHud"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.enableHud.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .withCallback(this::setEnableHud));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.exitConfirmation"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.closePrompt.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .withBounding(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 225, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .withCallback(this::setExitConfirmation));
    }

    private void setExitConfirmation(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        ClientConfiguration.closePrompt.setValue(booleanEnumCycleButton.getValue() == BooleanEnum.TRUE);
        ClientConfiguration.save();
    }

    private void setEnableHud(CycleButton<BooleanEnum> cycleButton) {
        ClientConfiguration.enableHud.setValue(cycleButton.getValue() == BooleanEnum.TRUE);
        ClientConfiguration.save();
    }

    private void setEnableCrosshair(CycleButton<BooleanEnum> cycleButton) {
        ClientConfiguration.enableCrosshair.setValue(cycleButton.getValue() == BooleanEnum.TRUE);
        ClientConfiguration.save();
    }

    private void setHideFirstPersonPlayer(CycleButton<BooleanEnum> value) {
        ClientConfiguration.firstPersonPlayerModel.setValue(value.getValue() == BooleanEnum.FALSE);
        ClientConfiguration.save();
    }

    private void setHideHotbarWhenThirdPerson(CycleButton<BooleanEnum> value) {
        ClientConfiguration.thirdpersonHotbar.setValue(value.getValue() == BooleanEnum.FALSE);
        ClientConfiguration.save();
    }

    private void setVibration(CycleButton<BooleanEnum> value) {
        ClientConfiguration.vibration.setValue(value.getValue() == BooleanEnum.TRUE);
        ClientConfiguration.save();
    }
}
