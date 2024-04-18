package com.ultreon.quantum.client.gui.screens.settings;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.config.Config;
import com.ultreon.quantum.client.gui.Alignment;
import com.ultreon.quantum.client.gui.Bounds;
import com.ultreon.quantum.client.gui.Position;
import com.ultreon.quantum.client.gui.screens.options.BooleanEnum;
import com.ultreon.quantum.client.gui.screens.tabs.TabBuilder;
import com.ultreon.quantum.client.gui.widget.CycleButton;
import com.ultreon.quantum.client.gui.widget.Label;
import com.ultreon.quantum.text.TextObject;

public class PrivacySettingsUI {
    static final TextObject TITLE = TextObject.translation("quantum.screen.options.privacy.title");
    private QuantumClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();

        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));
        
        builder.add(TextObject.translation("quantum.screen.options.privacy.hideActiveServer"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideActiveServerFromRPC ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.hidden") : TextObject.translation("quantum.ui.visible"))
                .callback(this::setHideActiveServer));

        builder.add(TextObject.translation("quantum.screen.options.privacy.hideRpc"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideRPC ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.hidden" : "quantum.ui.visible"))
                .callback(this::setHideActivity)
        );

        builder.add(TextObject.translation("quantum.screen.options.privacy.hidePlayerName"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideUsername ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("quantum.ui.hidden") : TextObject.translation("quantum.ui.visible"))
                .callback(this::setHidePlayerNames));

        builder.add(TextObject.translation("quantum.screen.options.privacy.hidePlayerSkin"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideSkin ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "quantum.ui.hidden" : "quantum.ui.visible"))
                .callback(this::setHidePlayerSkin));
    }

    private void setHideActiveServer(CycleButton<BooleanEnum> button) {
        Config.hideActiveServerFromRPC = button.getValue().get();
        this.client.newConfig.save();
    }
    
    private void setHideActivity(CycleButton<BooleanEnum> button) {
        Config.hideRPC = button.getValue().get();
        this.client.newConfig.save();
    }

    private void setHidePlayerNames(CycleButton<BooleanEnum> button) {
        Config.hideUsername = button.getValue().get();
        this.client.newConfig.save();
    }

    private void setHidePlayerSkin(CycleButton<BooleanEnum> button) {
        Config.hideSkin = button.getValue().get();
        this.client.newConfig.save();
    }
}
