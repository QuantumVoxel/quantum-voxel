package dev.ultreon.quantum.client.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.skript.QuantumSkript;

import java.io.IOException;

public class QuantumClientSkript extends QuantumSkript {
    private SkriptAddon addon;

    public QuantumClientSkript() {
        super(() -> QuantumClient.get().registries);
    }

    @Override
    public String getName() {
        return "QuantumClient";
    }


    @Override
    public void onEnable() {
        this.addon = Skript.registerAddon(this);
        try {
            this.addon.loadClasses("dev.ultreon.quantum.client.skript", "events", "expressions", "effects", "sections", "structures");
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load classes:", e);
        }

        this.registerEventValues();
        this.registerTypes();
    }

    private void registerTypes() {
        Classes.registerClass(new ClassInfo<>(Screen.class, "screen")
                .name("Screen")
                .description("Represents a menu or graphical screen with widgets like buttons, text fields, etc."));

        Classes.registerClass(new ClassInfo<>(Widget.class, "widget")
                .name("Screen Widget")
                .description("Represents a graphical widget. Like a button, text field, etc."));
    }

    private void registerEventValues() {
        // TODO
    }

    @Override
    public SkriptAddon getAddon() {
        return addon;
    }
}
