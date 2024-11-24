package dev.ultreon.quantum.api.commands;

import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.libs.commons.v0.util.ExceptionUtils;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.api.commands.error.InternalError;
import dev.ultreon.quantum.api.commands.error.NoPermissionError;
import dev.ultreon.quantum.api.commands.error.OverloadError;
import dev.ultreon.quantum.api.commands.output.CommandResult;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.chat.Chat;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandParserImpl {
    private static final List<CommandParserImpl> parsers = new ArrayList<>();
    
    public Class<? extends Command> clazz;
    public final CommandData data;
    public Set<CommandSpec> commandSpecs = new HashSet<>();


    /**
     * Mapping of (a pair of (a list of text parameters) and (an integer for parameter messageType count on the method)) to (a command runnable for invoke method on the method).
     */
    private final Map<Pair<List<String>, Integer>, CommandRunnable> textRunnableMap = new HashMap<>();
    final List<Overload> overloads = new ArrayList<>();
    
    public CommandParserImpl(Class<? extends Command> clazz, CommandData data, Set<CommandSpec> commandSpecs) {
        CommandParserImpl.addParser(this);
        this.clazz = clazz;
        this.data = data;
        this.loadOverloads(this.overloads, this.commandSpecs = commandSpecs);
        this.checkForConflicts(this.overloads);
    }

    private static void addParser(CommandParserImpl parser) {
        CommandParserImpl.parsers.add(parser);
    }

    private void loadOverloads(List<Overload> overloads, Set<CommandSpec> commandSpecs) {
        overloads.clear();
        for (var commandSpec : commandSpecs) {
            overloads.add(new Overload(commandSpec.arguments(), commandSpec, this.data.mapToPerm(commandSpec)));
        }
    }

    private void checkForConflicts(List<Overload> overloads) {
        // Found overloads.
        final Map<OverloadContent, Overload> conflicts = new HashMap<>();
        final List<Pair<Overload, CommandSpecValues>> duplicateSpecValues = new ArrayList<>();
        final List<OverloadContent> overloadsContents = new ArrayList<>();

        // Check each overload.
        for (var overload : this.overloads) {
            final var keys = new IndexedCommandSpecValues();
            int size;

            // Loop all arguments.
            final var i = new AtomicInteger();
            for (var param : overload.args()) {
                if (param instanceof CommandParameter.Text text) {
                    String[] strings = text.getText();
                    final var values = new CommandSpecValues();
                    final var duplicateValues = new CommandSpecValues();
                    for (var s : strings) {
                        if (s.isEmpty()) {
                            continue;
                        }
                        if (values.contains(s)) {
                            duplicateValues.add(s);
                        } else {
                            values.add(s);
                        }
                    }
                    if (!duplicateValues.isEmpty()) {
                        duplicateSpecValues.add(new Pair<>(overload, duplicateValues));
                    }
                    keys.set(i.get(), values);
                } else if (param instanceof CommandParameter.ArgumentType) {
                    keys.set(i.get(), null);
                }
                i.getAndIncrement();
            }
            size = i.get();
            final var overloadContent = new OverloadContent(overload.spec().commandName(), size, keys);
            if (overloadsContents.contains(overloadContent)) {
                if (!conflicts.containsKey(overloadContent)) {
                    conflicts.put(overloadContent, overload);
                }
            } else {
                overloadsContents.add(overloadContent);
            }
        }

        if (!duplicateSpecValues.isEmpty()) {
            QuantumServer.LOGGER.warn("Duplicate specification values for commands: {}", StringUtils.join(this.data.getAliases(), ", "));
            for (var duplicateSpecValue : duplicateSpecValues) {
                QuantumServer.LOGGER.warn("  Overload: {}", duplicateSpecValue.getFirst().spec().toString());
                for (var duplicate : duplicateSpecValue.getSecond()) {
                    QuantumServer.LOGGER.warn("    Duplicate: {}", duplicate);
                }
            }
        }
        if (!conflicts.isEmpty()) {
            if (overloadsContents.size() <= 1) return;

            QuantumServer.LOGGER.error("FATAL: Overload conflict for commands: {}", StringUtils.join(this.data.getAliases(), ", "));
            for (var entry : conflicts.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                QuantumServer.LOGGER.error("FATAL:   Conflict: {}", value.spec());
                QuantumServer.LOGGER.error("FATAL:     Conflicting arguments: {}", StringUtils.join(
                        key.indexedValues.values(), ", "
                ));
                QuantumServer.LOGGER.error("FATAL:     Conflicting size: {}", StringUtils.join(
                        key.indexedValues.values(), ", "
                ));
            }
        }
    }

    public @Nullable CommandResult execute(
        Command instance,
        CommandSender sender,
        CommandContext commandCtx,
        String alias,
        String[] args
    ) throws CommandParseException {
        // Initial parameter types.
        final List<Class<?>> paramTypesList = new ArrayList<>();
        paramTypesList.add(0, CommandSender.class);
        paramTypesList.add(1, CommandContext.class);
        paramTypesList.add(2, String.class);

        // Found overloads.
        final List<Overload> foundOverloads = new ArrayList<>();

        // Check each overload.
        for (var overload : this.overloads) {
            if (!overload.validFor(sender, commandCtx, args)) continue;
            foundOverloads.add(overload);
        }

        // If there's multiple overloads available for this set of command arguments.
        if (foundOverloads.size() > 1) {
            if (sender instanceof Player) {
                if (sender.hasPermission("quantum.developer") && GamePlatform.get().isDevEnvironment()) {
                    Chat.sendError(sender, "Multiple overloads have been found.");
                }
                Chat.sendError(sender, "Error occurred when parsing the command. Code: CP-0001");
            }
            return null;
        }
        if (foundOverloads.isEmpty()) {
            return new OverloadError();
        }

        // Get overload, and add all parameter types
        final var overload = foundOverloads.get(0);
        if (!overload.hasPermission(sender)) {
            return new NoPermissionError();
        }

        // Get transformed arguments, arguments transformed from the old string arguments.
        final var callArgs = new ArrayList<>(Objects.requireNonNull(overload.getObjectCache()));

        // Final objects.
        callArgs.addAll(0, List.of(sender, commandCtx, alias));

        // Get overloaded method.
        final var method = this.data.mapToMethod(overload.spec());
        if (method == null) return new InternalError("Execution doesn't exists.");

        // Invoke method and return command output.
        try {
            return (CommandResult) method.invoke(instance, callArgs.toArray(new Object[0]));
        } catch (IllegalAccessException e) {
            QuantumServer.LOGGER.error("Failed to access command method: {}", method);
            return null;
        } catch (InvocationTargetException e) {
            QuantumServer.LOGGER.error("Failed to invoke command method: {}", method, e);
            return null;
        } catch (IllegalArgumentException e) {
            QuantumServer.LOGGER.error("Got illegal argument, possible argument mismatch.");
            QuantumServer.LOGGER.error("Dumping method argument types:");
            for (var type : method.getParameterTypes()) QuantumServer.LOGGER.error("  {}", type.getName());

            QuantumServer.LOGGER.error("Dumping called argument types:");
            for (var obj : callArgs) QuantumServer.LOGGER.error("  {}", obj == null ? null : obj.getClass().getName());

            QuantumServer.LOGGER.error("Dumping stack trace:");
            for (var line : dev.ultreon.libs.commons.v0.util.StringUtils.splitIntoLines(ExceptionUtils.getStackTrace(e)))
                QuantumServer.LOGGER.error("  {}", line);
            throw new CommandArgumentMismatch(method.getParameterTypes(), callArgs, e);
        }
    }

    public List<String> tabComplete(CommandSender sender, CommandContext commandCtx, String alias, String[] args) {
        // Check each overload.
        final List<String> result = new ArrayList<>();
        for (var overload : this.overloads) {
            final var completion = overload.tabComplete(sender, commandCtx, args, new ArrayList<>());
            if (completion != null) {
                result.addAll(completion);
            }
        }
        return result;
    }
}