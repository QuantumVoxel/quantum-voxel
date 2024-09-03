package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.util.NamespaceID;

public record C2SCraftRecipePacket(int typeId, NamespaceID recipeId) implements Packet<InGameServerPacketHandler> {
    public C2SCraftRecipePacket(RecipeType<?> type, Recipe recipe) {
        this(type.getId(), recipe.getId());
    }
    public static C2SCraftRecipePacket read(PacketIO buffer) {
        int typeId = buffer.readInt();
        NamespaceID recipeId = buffer.readId();
        return new C2SCraftRecipePacket(typeId, recipeId);
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
