package dev.ultreon.quantum.client.gui.screens.container;

import dev.ultreon.quantum.menu.AdvancedCraftingMenu;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.packets.c2s.C2SCraftAdvancedRecipePacket;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

public class AdvancedCraftingScreen extends InventoryScreen {
    public AdvancedCraftingScreen(AdvancedCraftingMenu menu, TextObject title) {
        super(menu, title);
    }

    @Override
    protected boolean isAdvanced() {
        return true;
    }

    @Override
    protected @NotNull Packet<InGameServerPacketHandler> getPacket(Recipe recipe) {
        return new C2SCraftAdvancedRecipePacket(recipe.getType(), recipe);
    }
}
