package dev.ultreon.quantum.network.packets.s2c;

import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.network.EncoderException;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;

public final class S2CRecipeSyncPacket<T extends Recipe> implements Packet<InGameClientPacketHandler> {
    private final RecipeType<T> type;
    private final ObjectMap<NamespaceID, ? extends T> recipes;

    public S2CRecipeSyncPacket(RecipeType<T> type,
                               ObjectMap<NamespaceID, ? extends T> recipes) {
        this.type = type;
        this.recipes = recipes;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Recipe> S2CRecipeSyncPacket<T> read(PacketIO packetIO) {
        RecipeType<? extends Recipe> type = Registries.RECIPE_TYPE.get(packetIO.readId());
        ObjectMap<NamespaceID, ? extends T> recipes = (ObjectMap<NamespaceID, T>) packetIO.readObjectMap(PacketIO::readId, io -> Recipe.read(type, io));
        return (S2CRecipeSyncPacket<T>) new S2CRecipeSyncPacket(type, recipes);
    }

    @Override
    public void toBytes(PacketIO packetIO) {
        NamespaceID id = Registries.RECIPE_TYPE.getId(type);
        if (id == null) throw new EncoderException("Unknown recipe type: " + type);
        packetIO.writeId(id);
        packetIO.writeObjectMap(recipes, PacketIO::writeId, (io, recipe) -> Recipe.write(type, io, recipe));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRecipeSync(this);
    }

    public RecipeType<T> type() {
        return type;
    }

    public ObjectMap<NamespaceID, ? extends T> recipes() {
        return recipes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CRecipeSyncPacket) obj;
        return Objects.equals(this.type, that.type) &&
               Objects.equals(this.recipes, that.recipes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, recipes);
    }

    @Override
    public String toString() {
        return "S2CRecipeSyncPacket[" +
               "type=" + type + ", " +
               "recipes=" + recipes + ']';
    }

}
