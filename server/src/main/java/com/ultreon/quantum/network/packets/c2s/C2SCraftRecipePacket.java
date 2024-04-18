package com.ultreon.quantum.network.packets.c2s;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.network.server.InGameServerPacketHandler;
import com.ultreon.quantum.recipe.Recipe;
import com.ultreon.quantum.recipe.RecipeType;
import com.ultreon.quantum.util.Identifier;

public class C2SCraftRecipePacket extends Packet<InGameServerPacketHandler> {
    private final int typeId;
    private final Identifier recipeId;

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
