package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.commands.Selections;
import dev.ultreon.quantum.api.neocommand.*;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.EntityHit;
import dev.ultreon.quantum.util.Hit;

import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerArgumentType implements ArgumentType<ServerPlayer> {
    private final boolean includeSelf;

    private PlayerArgumentType() {
        this(true);
    }

    private PlayerArgumentType(boolean includeSelf) {
        this.includeSelf = includeSelf;
    }

    @SuppressWarnings("t")
    @Override
    public ServerPlayer parse(CommandReader ctx) throws CommandParseException {
        Selector<?> selector = ctx.nextSelector();
        Selector.Type<?> type = selector.type();
        if (type == Selector.Type.ID) {
            return ctx.getServer().getEntity((int) selector.value());
        } else if (type == Selector.Type.NAME) {
            return ctx.getServer().getPlayer((String) selector.value());
        } else if (type == Selector.Type.UUID) {
            return ctx.getServer().getPlayer((UUID) selector.value());
        } else if (includeSelf && type == Selector.Type.TAG) {
            switch ((String) selector.value()) {
                case "me":
                    if (ctx.getSender() instanceof ServerPlayer) {
                        ServerPlayer player = (ServerPlayer) ctx.getSender();
                        return player;
                    } else throw new CommandParseException("You are not a player", ctx.tell());
                case "selection":
                    Selections selections = Selections.get(ctx.getSender());
                    return (ServerPlayer) selections.getPlayer();
                case "target":
                    if (ctx.getSender() instanceof ServerPlayer) {
                        ServerPlayer player = (ServerPlayer) ctx.getSender();
                        Hit hit = player.rayCast();
                        if (hit instanceof EntityHit) {
                            Entity entity = ((EntityHit) hit).getEntity();
                            if (entity instanceof ServerPlayer) {
                                ServerPlayer player1 = (ServerPlayer) entity;
                                return player1;
                            } else throw new CommandParseException("The target is not a player", ctx.tell());
                        } else {
                            throw new CommandParseException("You are not looking at a player", ctx.tell());
                        }
                    } else {
                        throw new CommandParseException("You are not a player", ctx.tell());
                    }
                default:
                    throw new CommandParseException("Unknown tag: " + selector.value(), ctx.tell());
            }
        } else if (type == Selector.Type.VARIABLE) {
            String var = (String) selector.value();
            if (ctx.getSender() instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer) ctx.getSender();
                Object variable = player.getVariable(var);
                if (variable == null) {
                    throw new CommandParseException("Variable not found: " + var, ctx.tell());
                }
                if (!(variable instanceof ServerPlayer)) {
                    throw new CommandParseException("Variable is not a player: " + var, ctx.tell());
                }
                return (ServerPlayer) variable;
            } else {
                throw new CommandParseException("You are not a player", ctx.tell());
            }
        } else {
            throw new CommandParseException("Unknown selector type: " + type, ctx.tell());
        }
    }

    @Override
    public void complete(SuggestionProvider ctx) {
        if (ctx.getCurrent().isEmpty()) {
            ctx.suggest(Selector.Type.ID, Selector.Type.NAME, Selector.Type.UUID, Selector.Type.TAG, Selector.Type.VARIABLE);
        } else if (ctx.getCurrent().charAt(0) == Selector.Type.TAG.character) {
            ctx.suggest("me", "selection", "target");
        } else if (ctx.getCurrent().charAt(0) == Selector.Type.VARIABLE.character) {
            if (ctx.getSender() instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer) ctx.getSender();
                ctx.suggest(player.getVariableNames(ServerPlayer.class));
            }
        } else if (ctx.getCurrent().charAt(0) == Selector.Type.ID.character) {
            List<Entity> entities = ctx.getServer().getEntities().collect(Collectors.toList());
            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    ctx.suggest(player.getId());
                }
            }
        } else if (ctx.getCurrent().charAt(0) == Selector.Type.NAME.character) {
            Collection<ServerPlayer> players = ctx.getServer().getPlayers();
            for (ServerPlayer player : players) {
                ctx.suggest(player.getName());
            }
        } else if (ctx.getCurrent().charAt(0) == Selector.Type.UUID.character) {
            Collection<ServerPlayer> players = ctx.getServer().getPlayers();
            for (ServerPlayer player : players) {
                ctx.suggest(player.getUuid().toString());
            }
        }
    }

    @Override
    public boolean matches(CommandReader arg) {
        try {
            arg.nextSelector();
            return true;
        } catch (CommandParseException e) {
            return false;
        }
    }

    @Override
    public List<String> getExamples() {
        return List.of(
                "#me",
                "#selection",
                "#target",
                "%123",
                "@name",
                ":012345678-1234-1234-1234-123456789012",
                "$variable-name_123"
        );
    }

    public static Parameter<ServerPlayer> player(String name) { return new Parameter<>(name, new PlayerArgumentType(true)); }

    public static Parameter<ServerPlayer> player(String name, boolean includeSelf) { return new Parameter<>(name, new PlayerArgumentType(includeSelf)); }
}
