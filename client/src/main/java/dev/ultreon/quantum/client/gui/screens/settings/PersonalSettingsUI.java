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
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .text(TextObject.translation("quantum.screen.options.personalisation.blurRadius.text"))
                .callback(this::setBlurRadius));
    }

    private void setBlurRadius(Slider integerCycleButton) {
        ClientConfig.blurRadius = (float) integerCycleButton.value().get();
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
