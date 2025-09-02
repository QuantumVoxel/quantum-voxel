package dev.ultreon.quantum.network.packets.c2s;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;

public final class C2SCraftRecipePacket implements Packet<InGameServerPacketHandler> {
    private final int typeId;
    private final NamespaceID recipeId;

    public C2SCraftRecipePacket(int typeId, NamespaceID recipeId) {
        this.typeId = typeId;
        this.recipeId = recipeId;
    }

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

    public int typeId() {
        return typeId;
    }

    public NamespaceID recipeId() {
        return recipeId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (C2SCraftRecipePacket) obj;
        return this.typeId == that.typeId &&
               Objects.equals(this.recipeId, that.recipeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId, recipeId);
    }

    @Override
    public String toString() {
        return "C2SCraftRecipePacket[" +
               "typeId=" + typeId + ", " +
               "recipeId=" + recipeId + ']';
    }

}
