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

public class PrivacySettingsUI {
    static final TextObject TITLE = TextObject.translation("quantum.screen.options.privacy.title");

    public void build(TabBuilder builder) {

        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));
        
        builder.add(TextObject.translation("quantum.screen.options.privacy.hideActiveServer"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.hideServerFromActivity.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.hidden") : TextObject.translation("quantum.ui.visible"))
                .setCallback(this::setHideActiveServer));

        builder.add(TextObject.translation("quantum.screen.options.privacy.hideRpc"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.hideActivity.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.hidden" : "quantum.ui.visible"))
                .setCallback(this::setHideActivity)
        );

        builder.add(TextObject.translation("quantum.screen.options.privacy.hidePlayerName"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.hideUsername.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.hidden") : TextObject.translation("quantum.ui.visible"))
                .setCallback(this::setHidePlayerNames));

        builder.add(TextObject.translation("quantum.screen.options.privacy.hidePlayerSkin"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(ClientConfiguration.hideSkin.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.hidden" : "quantum.ui.visible"))
                .setCallback(this::setHidePlayerSkin));
    }

    private void setHideActiveServer(CycleButton<BooleanEnum> button) {
        ClientConfiguration.hideServerFromActivity.setValue(button.getValue().get());
        ClientConfiguration.save();
    }
    
    private void setHideActivity(CycleButton<BooleanEnum> button) {
        ClientConfiguration.hideActivity.setValue(button.getValue().get());
        ClientConfiguration.save();
    }

    private void setHidePlayerNames(CycleButton<BooleanEnum> button) {
        ClientConfiguration.hideUsername.setValue(button.getValue().get());
        ClientConfiguration.save();
    }

    private void setHidePlayerSkin(CycleButton<BooleanEnum> button) {
        ClientConfiguration.hideSkin.setValue(button.getValue().get());
        ClientConfiguration.save();
    }
}
