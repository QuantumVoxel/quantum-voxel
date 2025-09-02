package dev.ultreon.quantum.api.neocommand.params;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.Selections;
import dev.ultreon.quantum.api.commands.variables.PlayerVariables;
import dev.ultreon.quantum.api.neocommand.*;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.EntityHit;
import dev.ultreon.quantum.util.Hit;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EntitiesArgumentType implements ArgumentType<List<Entity>> {
    private final Predicate<Entity> predicate;

    private EntitiesArgumentType(Predicate<Entity> predicate) {
        this.predicate = predicate;
    }

    @SuppressWarnings({"t", "unchecked"})
    @Override
    public List<Entity> parse(CommandReader ctx) throws CommandParseException {
        Selector<?> selector = ctx.nextSelector();
        Selector.Type<?> type = selector.type();
        if (type == Selector.Type.ID) {
            Entity entity = ctx.getServer().getEntity((int) selector.value());
            if (entity == null || !predicate.test(entity)) {
                throw new CommandParseException("No entity with id " + selector.value(), ctx.tell());
            }
            return List.of(entity);
        } else if (type == Selector.Type.NAME) {
            List<Entity> collect = ctx.getServer().getEntities().filter(entity -> entity.getName().equals(selector.value()) && predicate.test(entity)).collect(Collectors.toList());
            if (collect.isEmpty()) {
                throw new CommandParseException("No entity with name '" + selector.value() + "'", ctx.tell());
            }
            return collect;
        } else if (type == Selector.Type.TAG) {
            switch ((String) selector.value()) {
                case "me":
                    if (ctx.getSender() instanceof Entity) {
                        Entity entity = (Entity) ctx.getSender();
                        if (!predicate.test(entity)) {
                            throw new CommandParseException("You don't seem to pass the conditions", ctx.tell());
                        }
                        return List.of(entity);
                    } else throw new CommandParseException("You are not an entity", ctx.tell());
                case "selection":
                    Selections selections = Selections.get(ctx.getSender());
                    Entity entity = selections.getEntity();
                    if (entity == null) {
                        throw new CommandParseException("No entity selected", ctx.tell());
                    }
                    if (!predicate.test(entity)) {
                        throw new CommandParseException("Selected entity doesn't pass the conditions", ctx.tell());
                    }
                    return List.of(entity);
                case "target":
                    if (ctx.getSender() instanceof Player) {
                        Player player = (Player) ctx.getSender();
                        Hit hit = player.rayCast();
                        if (hit instanceof EntityHit) {
                            Entity entity1 = ((EntityHit) hit).getEntity();
                            if (!predicate.test(entity1)) {
                                throw new CommandParseException("The target doesn't pass the conditions", ctx.tell());
                            }
                            return List.of(entity1);
                        } else {
                            throw new CommandParseException("You are not looking at an entity", ctx.tell());
                        }
                    } else throw new CommandParseException("You are not an player", ctx.tell());
                default:
                    throw new CommandParseException("Invalid entity tag: " + selector.value(), ctx.tell());
            }
        } else if (type == Selector.Type.UUID) {
            List<Entity> collect = ctx.getServer().getEntities().filter(entity -> entity.getUuid().equals(selector.value())).collect(Collectors.toList());
            if (collect.isEmpty())
                throw new CommandParseException("No entity with uuid " + selector.value(), ctx.tell());
            if (collect.size() > 1)
                throw new CommandParseException("Multiple entities with uuid " + selector.value(), ctx.tell());
            if (!predicate.test(collect.get(0)))
                throw new CommandParseException("Entity with uuid " + selector.value() + " doesn't pass the conditions", ctx.tell());
            return collect;
        } else if (type == Selector.Type.VARIABLE) {
            CommandSender sender = ctx.getSender();
            if (!(sender instanceof Player)) throw new CommandParseException("You are not a player", ctx.tell());
            PlayerVariables variables = PlayerVariables.get((ServerPlayer) sender);
            Object variable = variables.getVariable((String) selector.value());
            if (variable instanceof Entity) {
                Entity entity = (Entity) variable;
                if (!predicate.test(entity)) {
                    throw new CommandParseException("Variable '" + selector.value() + "' doesn't pass the conditions", ctx.tell());
                }
                return List.of(entity);
            }
            else if (variable instanceof List<?>) {
                List<?> list = (List<?>) variable;
                List<Entity> collect = (List<Entity>) list.stream().filter(entity -> entity instanceof Entity).collect(Collectors.toList());
                if (collect.isEmpty())
                    throw new CommandParseException("No entity in list variable '" + selector.value() + "'", ctx.tell());
                return collect;
            }
            else throw new CommandParseException("Variable '" + selector.value() + "' is not an entity or a list of entities", ctx.tell());
        } else {
            throw new CommandParseException("Invalid selector: " + type.character, ctx.tell());
        }
    }

    @Override
    public void complete(SuggestionProvider ctx) {
        // do nothing
    }

    @Override
    public boolean matches(CommandReader arg) {
        if (arg.isEOF()) {
            return false;
        }

        try {
            arg.nextSelector();
            return true;
        } catch (CommandParseException e) {
            return false;
        }
    }

    @Override
    public List<String> getExamples() {
        return List.of("#me", "#selection", "#target", "%123", "@name", ":012345678-1234-1234-1234-123456789012", "$variable-name_123");
    }

    public static Parameter<List<Entity>> entities(String name) {
        return new Parameter<>(name, new EntitiesArgumentType(predicate -> true));
    }

    public static Parameter<List<Entity>> entitiesOf(String name, Predicate<Entity> predicate) {
        return new Parameter<>(name, new EntitiesArgumentType(predicate));
    }
}
