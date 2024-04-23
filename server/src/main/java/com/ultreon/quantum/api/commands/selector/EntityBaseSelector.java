package com.ultreon.quantum.api.commands.selector;

import com.ultreon.quantum.api.commands.CommandContext;
import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.api.commands.Selections;
import com.ultreon.quantum.api.commands.TabCompleting;
import com.ultreon.quantum.api.commands.error.*;
import com.ultreon.quantum.api.commands.variables.PlayerVariables;
import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("unused")
public class EntityBaseSelector<T extends Entity> extends BaseSelector<T> {
    private final CommandSender sender;
    private final Class<T> clazz;

    public EntityBaseSelector(CommandSender sender, Class<T> clazz, Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.clazz = clazz;
        this.result = this.calculateData();
    }

    public EntityBaseSelector(CommandSender sender, Class<T> clazz, String text) {
        super(text);
        this.sender = sender;
        this.clazz = clazz;
        this.result = this.calculateData();
    }

    public static  <T extends Entity> SelectorFactory<EntityBaseSelector<T>> create(Class<T> clazz) {
        return (sender, text) -> new EntityBaseSelector<T>(sender, clazz, text);
    }

    @SuppressWarnings({"RedundantVariableInitialization"})
    @Override
    public Result<T> calculateData() {
        Player player = null;
        if (this.sender instanceof Player) {
            player = (Player) this.sender;
        }
        if (this.getError() != null) {
            return new Result<>(null, this.getError());
        }
        Object target;
        return result =  switch (this.getKey()) {
            case TAG -> tag(player);
            case UUID -> uuid();
            case NAME -> name();
            case ID -> id();
            case VARIABLE -> variable(player);
            default -> new Result<>(null, new OverloadError());
        };
    }

    private @NotNull Result<T> variable(Player player) {
        if (player == null) return new Result<>(null, new NeedPlayerError());

        Object target = PlayerVariables.get((ServerPlayer) player).getVariable(this.stringValue);
        if (target == null) return new Result<>(null, new NotFoundError("variable " + this.stringValue));
        if (!clazz.isInstance(target)) return new Result<>(null, new InvalidVariableError(this.stringValue));
        return new Result<>(clazz.cast(target), null);
    }

    private @NotNull Result<T> id() {
        try {
            int id = Integer.parseInt(this.getStringValue());
            @Nullable var target = QuantumServer.get().getEntity(id);
            if (!clazz.isInstance(target)) return new Result<>(null, new NotFoundError("entity with id " + id));

            return new Result<>(clazz.cast(target), null);
        } catch (NumberFormatException e) {
            return new Result<>(null, new InvalidIntegerError(this.getStringValue()));
        }
    }

    private @NotNull Result<T> name() {
        if (Player.class.isAssignableFrom(clazz)) return new Result<>(null, new InvalidTargetError("an entity that is not a player"));

        Object target = QuantumServer.get().getPlayer(this.getStringValue());
        if (!clazz.isInstance(target)) return new Result<>(null, new NotFoundError("player with name " + this.getStringValue()));

        return new Result<>(clazz.cast(target), null);
    }

    private @NotNull Result<T> uuid() {
        Object target;
        UUID uuid = null;
        try {
            uuid = UUID.fromString(this.getStringValue());
            target = QuantumServer.get().getEntity(uuid);
            return target != null
                    ? new Result<>(clazz.cast(target), null)
                    : new Result<>(null, new NotFoundError("entity with uuid " + uuid));
        } catch (IllegalArgumentException e) {
            return uuid == null
                    ? new Result<>(null, new InvalidUUIDError())
                    : new Result<>(null, new ImpossibleError("Got error that couldn't be caught."));
        }
    }

    private @NotNull Result<T> tag(Player player) {
        Object target;
        var stringValue = this.getStringValue();
        if (stringValue == null) return new Result<>(null, new OverloadError());

        switch (stringValue) {
            case "target" -> {
                if (player == null) return new Result<>(null, new NeedPlayerError());

                target = player.rayCast(player.getWorld().getEntitiesByClass(this.clazz));
                try {
                    return new Result<>(clazz.cast(target), null);
                } catch (ClassCastException e) {
                    return new Result<>(null, new TargetEntityNotFoundError(this.clazz.getSimpleName()));
                }
            }
            case "me" -> {
                return this.clazz.isAssignableFrom(this.sender.getClass())
                        ? new Result<>(clazz.cast(this.sender), null)
                        : new Result<T>(null, new NeedEntityError());
            }
            case "nearest" -> {
                if (player == null) return new Result<>(null, new NeedPlayerError());

                target = player.nearestEntity(this.clazz);
                return target == null
                        ? new Result<>(null, new NotFoundInWorldError("entity"))
                        : new Result<>(clazz.cast(target), null);
            }
            case "selected" -> {
                var entity = Selections.get(this.sender).getEntity();
                return this.clazz.isInstance(entity)
                        ? new Result<>(clazz.cast(entity), null)
                        : new Result<T>(null, new NoSelectedError("entity"));
            }
            default -> {
                return new Result<>(null, new OverloadError());
            }
        }
    }

    public static ArrayList<String> tabComplete(Class<? extends Entity> clazz, CommandSender sender, CommandContext commandCtx, String arg) {
        return tabComplete(clazz, true, sender, commandCtx, arg);
    }

    @SuppressWarnings("UnusedParameters")
    public static ArrayList<String> tabComplete(Class<? extends Entity> clazz, boolean canBeSender, CommandSender sender, CommandContext commandCtx, String arg) {
        var output = new ArrayList<String>();
        if (sender instanceof ServerPlayer) {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "target", "nearest", "selected");
            if (canBeSender) TabCompleting.selectors(output, SelectorKey.TAG, arg, "me");
            TabCompleting.selectors(output, SelectorKey.VARIABLE, arg, TabCompleting.variables(new ArrayList<>(), "", (ServerPlayer) sender, ServerPlayer.class));
        } else TabCompleting.selectors(output, SelectorKey.TAG, arg, "selected");

        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.entityUuids(new ArrayList<>(), "", clazz));
        return output;
    }
}