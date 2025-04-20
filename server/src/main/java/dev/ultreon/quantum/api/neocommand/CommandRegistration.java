package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.entity.player.Player;

import java.util.List;

import static dev.ultreon.quantum.api.neocommand.params.CommandArgumentType.commands;
import static dev.ultreon.quantum.api.neocommand.params.EntitiesArgumentType.entitiesOf;
import static dev.ultreon.quantum.api.neocommand.params.IntArgumentType.ints;
import static dev.ultreon.quantum.api.neocommand.params.StringArgumentType.strings;

public class CommandRegistration {
    public static void register() {
        killCommand();
        teleportCommand();
        sayCommand();
        helpCommand();
    }

    private static void killCommand() {
        Commands.register("kill")
                .overload(ctx -> {
                    CommandSender sender = ctx.sender();
                    if (!(sender instanceof LivingEntity livingEntity))
                        return BasicCommandResult.error("You must be a living entity to use this command");

                    livingEntity.kill();
                    return BasicCommandResult.success("You successfully killed yourself (wait why?)");
                })
                .overload(ctx -> {
                    List<LivingEntity> entity = ctx.get("entity");
                    if (entity.isEmpty())
                        return BasicCommandResult.error("You must specify at least one entity to kill");

                    entity.forEach(LivingEntity::kill);
                    return BasicCommandResult.success("You successfully killed " + entity.size() + " entities");
                }, entitiesOf("entity", entity -> entity instanceof LivingEntity));
    }

    private static void teleportCommand() {
        Commands.register("tp")
                .overload(ctx -> {
                    CommandSender sender = ctx.sender();
                    if (!(sender instanceof LivingEntity livingEntity))
                        return BasicCommandResult.error("You must be a living entity to use this command");

                    List<LivingEntity> entity = ctx.get("entity");
                    if (entity.isEmpty())
                        return BasicCommandResult.error("You must specify at least one entity to teleport to");

                    if (entity.size() > 1)
                        return BasicCommandResult.error("You can only teleport to one entity at a time");

                    LivingEntity target = entity.getFirst();
                    livingEntity.teleportTo(target.getX(), target.getY(), target.getZ());
                    return BasicCommandResult.success("You successfully teleported to " + target.getName());
                }, entitiesOf("entity", entity -> entity instanceof LivingEntity))
                .overload(context -> {
                    CommandSender sender = context.sender();
                    if (!(sender instanceof LivingEntity livingEntity))
                        return BasicCommandResult.error("You must be a living entity to use this command");

                    int x = context.get("x");
                    int y = context.get("y");
                    int z = context.get("z");
                    livingEntity.teleportTo(x, y, z);
                    return BasicCommandResult.success("You successfully teleported to " + x + ", " + y + ", " + z);
                }, ints("x"), ints("y"), ints("z"));
    }

    private static void sayCommand() {
        Commands.register("say")
                .overload(ctx -> {
                    String message = ctx.get("message");
                    for (Player player : ctx.server().getPlayers()) {
                        player.sendMessage("[cyan]<" + ctx.sender().getName() + "> [white]" + message);
                    }
                    return BasicCommandResult.success("You successfully sent a message to all players");
                }, strings("message"));
    }

    private static void helpCommand() {
        Commands.register("help")
                .overload(ctx -> {
                    String command = ctx.get("command");

                    CommandRegistrant commandRegistrant = Commands.getCommand(command);
                    if (commandRegistrant == null)
                        return BasicCommandResult.error("Unknown command: " + command);

                    String description = commandRegistrant.getDescription();
                    if (description == null)
                        return BasicCommandResult.error("Unknown command: " + command);

                    return BasicCommandResult.success(description);
                }, commands("command"));
    }
}
