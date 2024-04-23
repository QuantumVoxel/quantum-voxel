package com.ultreon.quantum.api.commands.selector;

import com.ultreon.quantum.api.commands.CommandContext;
import com.ultreon.quantum.api.commands.CommandSender;
import com.ultreon.quantum.api.commands.Selections;
import com.ultreon.quantum.api.commands.TabCompleting;
import com.ultreon.quantum.api.commands.error.*;
import com.ultreon.quantum.api.commands.variables.PlayerVariables;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.server.QuantumServer;
import com.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerBaseSelector extends BaseSelector<Player> {

    private final CommandSender sender;

    public PlayerBaseSelector(CommandSender sender, BaseSelector.Parsed parsed) {
        super(parsed);
        this.sender = sender;
        this.result = this.calculateData();
    }

    public PlayerBaseSelector(CommandSender sender, String text) {
        super(text);
        this.sender = sender;
        this.result = this.calculateData();
    }

    @Override
    public Result<Player> calculateData() {
        Player player = null;
        if (this.sender instanceof Player) {
            player = (Player) this.sender;
        }
        if (this.error != null) {
            return new Result<>(null, this.error);
        }
        switch (this.key) {
            case TAG -> {
                return switchTag(player);
            }
            case NAME -> {
                return name();
            }
            case UUID -> {
                return uuid();
            }
            case ID -> {
                return id();
            }
            case VARIABLE -> {
                return variable(player);
            }
            default -> {
                return new Result<>(null, new OverloadError());
            }
        }
    }

    private Result<Player> variable(Player player) {
        if (player == null) return new Result<>(null, new NeedPlayerError());

        Object target = PlayerVariables.get((ServerPlayer) player).getVariable(this.stringValue);
        if (target == null) return new Result<>(null, new NotFoundError("variable " + this.stringValue));
        if (!(target instanceof Player)) return new Result<>(null, new InvalidVariableError(this.stringValue));
        return new Result<>((Player) target, null);
    }

    private @NotNull Result<Player> id() {
        try {
            int id = Integer.parseInt(this.stringValue);
            var target = QuantumServer.get().<Player>getEntity(id);
            if (target == null) {
                return new Result<>(null, new NotFoundError("player with id " + id));
            }
            return new Result<>(target, null);
        } catch (NumberFormatException e) {
            return new Result<>(null, new InvalidIntegerError(this.stringValue));
        }
    }

    private @NotNull Result<Player> uuid() {
        Player target;
        try {
            UUID uuid = UUID.fromString(this.stringValue);
            target = QuantumServer.get().getPlayer(uuid);
            if (target == null) {
                return new Result<>(null, new NotFoundError("player with uuid " + uuid));
            }
            return new Result<>(target, null);
        } catch (IllegalArgumentException e) {
            return new Result<>(null, new InvalidUUIDError());
        }
    }

    private @NotNull Result<Player> name() {
        Player target;
        String name = this.stringValue;
        target = QuantumServer.get().getPlayer(name);
        if (target instanceof ServerPlayer) {
            return new Result<>(target, null);
        } else {
            return new Result<>(null, new NotFoundError("player " + name));
        }
    }

    private @NotNull Result<Player> switchTag(Player player) {
        Object target0;
        switch (this.stringValue) {
            case "target" -> {
                if (player == null) {
                    return new Result<>(null, new NeedPlayerError());
                }
                target0 = player.rayCast(player.getWorld().getEntities());
                if (target0 == null) {
                    return new Result<>(null, new TargetPlayerNotFoundError());
                }
                return (target0 instanceof Player)
                        ? new Result<>((Player) target0, null)
                        : new Result<>(null, new TargetPlayerNotFoundError());
            }
            case "me" -> {
                return (this.sender instanceof Player)
                        ? new Result<>((Player) this.sender, null)
                        : new Result<>(null, new NeedPlayerError());
            }
            case "nearest" -> {
                if (player == null) {
                    return new Result<>(null, new NeedPlayerError());
                }
                target0 = player.nearestEntity(Player.class);
                return (target0 == null)
                        ? new Result<>(null, new NotFoundInWorldError("player"))
                        : new Result<>((Player) target0, null);
            }
            case "selected" -> {
                target0 = Selections.get(this.sender).getPlayer();
                if (target0 == null) {
                    return new Result<>(null, new NoSelectedError("player"));
                }
                return new Result<>((Player) target0, null);
            }
            default -> {
                return new Result<>(null, new OverloadError());
            }
        }
    }

    public static ArrayList<String> tabComplete(CommandSender sender, CommandContext commandCtx, String arg) {
        return PlayerBaseSelector.tabComplete(true, sender, commandCtx, arg);
    }

    public static ArrayList<String> tabComplete(boolean canBeSender, CommandSender sender, CommandContext commandCtx, String arg) {
        ArrayList<String> output = new ArrayList<>();

        if (sender instanceof ServerPlayer) {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "target", "nearest", "selected");
            if (canBeSender) {
                TabCompleting.selectors(output, SelectorKey.TAG, arg, "me");
            }

            TabCompleting.selectors(output, SelectorKey.VARIABLE, arg, TabCompleting.variables(new ArrayList<>(), "", (ServerPlayer) sender, ServerPlayer.class));
            TabCompleting.selectors(output, SelectorKey.ID, arg, TabCompleting.entityIds(new ArrayList<>(), ((ServerPlayer) sender).getWorld(), ""));
        } else {
            TabCompleting.selectors(output, SelectorKey.TAG, arg, "selected");
        }

        TabCompleting.selectors(output, SelectorKey.NAME, arg, TabCompleting.onlinePlayers(new ArrayList<>(), ""));
        TabCompleting.selectors(output, SelectorKey.UUID, arg, TabCompleting.entityUuids(new ArrayList<>(), "", Player.class));

        return output;
    }
}