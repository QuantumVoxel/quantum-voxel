package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;
import io.netty.handler.codec.EncoderException;

import java.util.List;

public record S2CRecipeSyncPacket<T extends Recipe>(RecipeType<T> type,
                                                    List<T> recipes) implements Packet<InGameClientPacketHandler> {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Recipe> S2CRecipeSyncPacket<T> read(PacketIO packetIO) {
        RecipeType<? extends Recipe> type = Registries.RECIPE_TYPE.get(packetIO.readId());
        List<? extends Recipe> recipes = packetIO.readList(io -> Recipe.read(type, io));
        return (S2CRecipeSyncPacket<T>) new S2CRecipeSyncPacket(type, recipes);
    }

    @Override
    public void toBytes(PacketIO packetIO) {
        NamespaceID id = Registries.RECIPE_TYPE.getId(type);
        if (id == null) throw new EncoderException("Unknown recipe type: " + type);
        packetIO.writeId(id);
        packetIO.writeList(recipes, (io, recipe) -> Recipe.write(type, io, recipe));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRecipeSync(this);
    }
}
