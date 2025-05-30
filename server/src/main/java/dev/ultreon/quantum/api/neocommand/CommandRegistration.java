package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.entity.player.Player;

import java.util.List;

import static dev.ultreon.quantum.api.neocommand.params.CommandArgumentType.commands;
import static dev.ultreon.quantum.api.neocommand.params.EntitiesArgumentType.entitiesOf;
import static dev.ultreon.quantum.api.neocommand.params.IntArgumentType.ints;
import static dev.ultreon.quantum.api.neocommand.params.StringArgumentType.strings;

/**
 * The CommandRegistration class is responsible for registering and managing
 * server commands. It provides various commands with respective functionalities,
 * such as player interactions, teleportation, messaging, and help features.
 */
public class CommandRegistration {
    /**
     * Registers all available commands for the server.
     */
    public static void register() {
        killCommand();
        teleportCommand();
        sayCommand();
        helpCommand();
    }

    /**
     * Registers the "kill" command and defines its behavior.
     * <p>
     * The command results in a success message upon successful execution or an error message
     * depending on the context or validation failures.
     */
    private static void killCommand() {
        Commands.register("kill")
                .overload(ctx -> {
                    // Since there aren't any arguments for this overload,
                    // we just get the sender.
                    CommandSender sender = ctx.sender();
                    
                    // As the sender can also be the console or a non-living entity,
                    // we check whether the sender is a living entity.
                    if (!(sender instanceof LivingEntity))
                        return BasicCommandResult.error("You must be a living entity to use this command");
                    
                    // Cast to a living entity
                    LivingEntity livingEntity = (LivingEntity) sender;

                    // Just calling kill() should do it :D
                    livingEntity.kill();
                    
                    // So, the player just committed su-, wait, should I say that here?
                    return BasicCommandResult.success("You successfully killed yourself (wait why?)");
                })
                .overload(ctx -> {
                    // Get the entity to murder.
                    List<LivingEntity> entity = ctx.get("entity");
                    
                    if (entity.isEmpty())
                        // We are alone in this empty lifeless world,
                        // at least we got an error when running this command...
                        return BasicCommandResult.error("You must specify at least one entity to kill");

                    // Kill each living being in the list of living entities. Wait, isn't that genocide? :concern:
                    entity.forEach(LivingEntity::kill);
                    
                    // Return success result with the amount they killed. How merciless!
                    return BasicCommandResult.success("You successfully killed " + entity.size() + " entities");
                }, entitiesOf("entity", entity -> entity instanceof LivingEntity));
    }

    /**
     * Defines and registers the "tp" (short for "teleport") command.
     * <p>
     * The "tp" command allows living entities to teleport either to another living entity's location
     * or to a specific set of coordinates.
     * <p>
     * Validation is performed to ensure the command sender is a living entity,
     * and the provided arguments meet the requirements of the selected overload.
     * Applicable error messages are returned when validation fails.
     */
    private static void teleportCommand() {
        // Teleportation command
        Commands.register("tp")
                .overload(ctx -> {
                    // Get command sender and validate it's a living entity
                    CommandSender sender = ctx.sender();
                    if (!(sender instanceof LivingEntity))
                        return BasicCommandResult.error("You must be a living entity to use this command");

                    // Cast sender to LivingEntity since we validated the type
                    LivingEntity livingEntity = (LivingEntity) sender;

                    // Get target entities and validate at least one exists
                    List<LivingEntity> entity = ctx.get("entity");
                    if (entity.isEmpty())
                        return BasicCommandResult.error("You must specify at least one entity to teleport to");

                    // Validate only one target entity was specified 
                    if (entity.size() > 1)
                        return BasicCommandResult.error("You can only teleport to one entity at a time");

                    // Get the first (and only) target entity and teleport to its coordinates
                    LivingEntity target = entity.get(0);
                    livingEntity.teleportTo(target.getX(), target.getY(), target.getZ());
                    return BasicCommandResult.success("You successfully teleported to " + target.getName());
                }, entitiesOf("entity", entity -> entity instanceof LivingEntity))
                .overload(context -> {
                    // Get command sender and validate it's a living entity
                    CommandSender sender = context.sender();
                    if (!(sender instanceof LivingEntity))
                        return BasicCommandResult.error("You must be a living entity to use this command");
                    
                    // Cast sender to LivingEntity since we validated the type
                    LivingEntity livingEntity = (LivingEntity) sender;

                    // Get coordinates from command arguments 
                    int x = context.get("x");
                    int y = context.get("y");
                    int z = context.get("z");
                    
                    // Teleport living entity to specified coordinates
                    livingEntity.teleportTo(x, y, z);
                    
                    // Return success message with the coordinates
                    return BasicCommandResult.success("You successfully teleported to " + x + ", " + y + ", " + z);
                }, ints("x"), ints("y"), ints("z"));
    }

    /**
     * Registers the "say" command which broadcasts a message to all players on the server.
     * The message is sent in a formatted chat format showing the sender's name and text.
     */
    private static void sayCommand() {
        Commands.register("say")
                .overload(ctx -> {
                    // Get the message content from command arguments
                    String message = ctx.get("message");

                    // Send the formatted message to all currently connected players
                    for (Player player : ctx.server().getPlayers()) {
                        player.sendMessage("[cyan]<" + ctx.sender().getName() + "> [white]" + message);
                    }

                    // Return success result after broadcasting
                    return BasicCommandResult.success("You successfully sent a message to all players");
                }, strings("message")); // Register with single string parameter for the message
    }

    /**
     * Registers the "help" command which provides information about other commands.
     * The command takes a single command name argument and returns its description.
     * If the command doesn't exist or has no description, an error message is returned.
     */
    private static void helpCommand() {
        Commands.register("help")
                .overload(ctx -> {
                    // Get the command name from arguments
                    String command = ctx.get("command");

                    // Find the command registrant for the requested command
                    CommandRegistrant commandRegistrant = Commands.getCommand(command);
                    if (commandRegistrant == null)
                        return BasicCommandResult.error("Unknown command: " + command);

                    // Get the command description, return error if none exists
                    String description = commandRegistrant.getDescription();
                    if (description == null)
                        return BasicCommandResult.error("Unknown command: " + command);

                    // Return the command description as success result
                    return BasicCommandResult.success(description);
                }, commands("command")); // Register with single command parameter
    }
}
