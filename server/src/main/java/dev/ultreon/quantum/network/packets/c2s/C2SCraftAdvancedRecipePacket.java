package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.util.NamespaceID;

public record C2SCraftAdvancedRecipePacket(int typeId, NamespaceID recipeId) implements Packet<InGameServerPacketHandler> {
    public C2SCraftAdvancedRecipePacket(RecipeType<?> type, Recipe recipe) {
        this(type.getId(), recipe.getId());
    }
    public static C2SCraftAdvancedRecipePacket read(PacketIO buffer) {
        int typeId = buffer.readInt();
        NamespaceID recipeId = buffer.readId();
        return new C2SCraftAdvancedRecipePacket(typeId, recipeId);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(this.typeId);
        buffer.writeId(this.recipeId);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onCraftAdvancedRecipe(this.typeId, this.recipeId);
    }
}
