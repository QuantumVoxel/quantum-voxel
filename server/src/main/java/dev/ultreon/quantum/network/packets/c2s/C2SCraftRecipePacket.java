package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.util.NamespaceID;

public class C2SCraftRecipePacket extends Packet<InGameServerPacketHandler> {
    private final int typeId;
    private final NamespaceID recipeId;

    public C2SCraftRecipePacket(RecipeType type, Recipe recipe) {
        this.typeId = type.getId();
        this.recipeId = recipe.getId();
    }

    public C2SCraftRecipePacket(PacketIO buffer) {
        this.typeId = buffer.readInt();
        this.recipeId = buffer.readId();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.typeId);
        buffer.writeId(this.recipeId);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onCraftRecipe(this.typeId, this.recipeId);
    }
}
