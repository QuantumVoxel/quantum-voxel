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
                .getCallback(this::setHideFirstPersonPlayer));

        builder.add(TextObject.translation("quantum.screen.options.accessibility.hideHotbarWhenThirdPerson"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.hideHotbarWhenThirdPerson ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.hidden" : "quantum.ui.visible"))
                .getCallback(this::setHideHotbarWhenThirdPerson)
        );

        builder.add(TextObject.translation("quantum.screen.options.accessibility.vibration"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfig.vibration ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.enabled") : TextObject.translation("quantum.ui.disabled"))
                .getCallback(this::setVibration));
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
