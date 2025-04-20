package dev.ultreon.quantum.api.commands;

import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.ServerChunk;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class Selections {
    private @Nullable Player player = null;
    private @Nullable Entity entity = null;
    private @Nullable ServerWorld world = null;
    private @Nullable ServerChunk chunk = null;
    protected PositionCommand.PositionSelection positions = new PositionCommand.PositionSelection();
    private static final Map<CommandSender, Selections> selections = new HashMap<>();

    public @Nullable Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        this.entity = player;
    }

    public @Nullable Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.player = (entity instanceof Player) ? (Player) entity : null;
        this.entity = entity;
    }

    public static Selections get(CommandSender sender) {
        if (selections.containsKey(sender)) {
            return selections.get(sender);
        } else {
            Selections select = new Selections();
            selections.put(sender, select);
            return select;
        }
    }

    public @Nullable ServerWorld getWorld() {
        return world;
    }

    public void setWorld(ServerWorld world) {
        this.world = world;
    }

    public @Nullable ServerChunk getChunk() {
        return chunk;
    }

    public void setChunk(ServerChunk chunk) {
        this.chunk = chunk;
    }
}