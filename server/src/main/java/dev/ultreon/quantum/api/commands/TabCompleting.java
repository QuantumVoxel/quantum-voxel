package dev.ultreon.quantum.api.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.api.commands.selector.SelectorKey;
import dev.ultreon.quantum.api.commands.variables.PlayerVariables;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.gamerule.Rule;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.registry.CommandRegistry;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.CacheablePlayer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Difficulty;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TabCompleting {
    public static List<String> onlinePlayers(String currentArgument) {
        return TabCompleting.onlinePlayers(new ArrayList<>(), currentArgument);
    }

    public static List<String> onlinePlayers(List<String> list, String currentArgument) {
        for (var player : QuantumServer.get().getPlayers()) {
            TabCompleting.addIfStartsWith(list, player.getName(), currentArgument);
        }
        return list;
    }

    public static List<String> players(String currentArgument) {
        return TabCompleting.players(new ArrayList<>(), currentArgument);
    }

    public static List<String> players(List<String> list, String currentArgument) {
        List<CacheablePlayer> players = new ArrayList<>();
        players.addAll(QuantumServer.get().getCachedPlayers());
        players.addAll(QuantumServer.get().getPlayers());
        for (var player : players) {
            var name = player.getName();
            if (name != null) {
                TabCompleting.addIfStartsWith(list, name, currentArgument);
            }
        }
        return list;
    }

    public static List<String> offlinePlayers(List<String> list, String currentArgument) {
        var players = Lists.newArrayList(QuantumServer.get().getCachedPlayers());
        for (var player : players) {
            var name = player.getName();
            if (name != null) {
                TabCompleting.addIfStartsWith(list, name, currentArgument);
            }
        }
        return list;
    }

    public static List<String> offlinePlayerUuids(List<String> list, String currentArgument) {
        for (var player : QuantumServer.get().getCachedPlayers()) {
            UUID uuid = player.getUuid();

            if (uuid != null) {
                TabCompleting.addIfStartsWith(list, uuid.toString(), currentArgument);
            }
        }
        return list;
    }

    public static <T extends Rule<?>> List<String> ruleNames(List<String> list, List<T> rules, String currentArgument) {
        for (Rule<?> rule : rules) {
            TabCompleting.addIfStartsWith(list, rule.getKey(), currentArgument);
        }
        return list;
    }

    public static List<String> entityTypes(List<String> list, String currentArgument) {
        return entityTypes(list, currentArgument, true);
    }

    public static List<String> entityTypes(List<String> list, String currentArgument, boolean includePlayer) {
        for (var entityType : Registries.ENTITY_TYPE.entries()) {
            var key = entityType.key;
            if (!includePlayer && entityType.value != EntityTypes.PLAYER)
                continue;

            TabCompleting.addIfStartsWith(list, key, currentArgument);
        }
        return list;
    }

    public static List<String> biomes(List<String> list, String currentArgument) {
        for (var biome : QuantumServer.get().getRegistries().get(RegistryKeys.BIOME).keys()) {
            var key = biome.id().toString();
            TabCompleting.addIfStartsWith(list, key, currentArgument);
        }
        return list;
    }

    public static List<String> difficulties(List<String> list, String currentArgument) {
        for (Difficulty difficulty : Difficulty.values()) {
            String name = difficulty.name().toLowerCase();
            TabCompleting.addIfStartsWith(list, name, currentArgument);
        }
        return list;
    }

    public static List<String> entityUuids(List<String> list, String currentArgument) {
        for (World world : QuantumServer.get().getWorlds()) {
            for (Entity entity : world.getEntities().toArray(Entity.class)) {
                String uuid = entity.getUuid().toString();
                TabCompleting.addIfStartsWith(list, uuid, currentArgument);
            }
        }
        return list;
    }

    public static List<String> entityUuids(List<String> list, String currentArgument, Class<? extends Entity> instance) {
        for (World world : QuantumServer.get().getWorlds()) {
            for (Entity entity : world.getEntities().toArray(Entity.class)) {
                if (!instance.isInstance(entity)) {
                    continue;
                }
                String uuid = entity.getUuid().toString();
                TabCompleting.addIfStartsWith(list, uuid, currentArgument);
            }
        }
        return list;
    }

    public static List<String> blocks(List<String> list, String currentArgument) {
        for (RegistryKey<Block> id : Registries.BLOCK.keys()) {
            TabCompleting.addIfStartsWith(list, id.id(), currentArgument);
        }
        return list;
    }

    public static List<String> items(List<String> list, String currentArgument) {
        for (RegistryKey<Item> id : Registries.ITEM.keys()) {
            TabCompleting.addIfStartsWith(list, id.id(), currentArgument);
        }
        return list;
    }

    public static List<String> keys(String currentArgument, Collection<NamespaceID> keys) {
        List<String> list = new ArrayList<>();
        for (var key : keys) {
            TabCompleting.addIfStartsWith(list, key.toString(), currentArgument);
        }
        return list;
    }

    public static List<String> worlds(List<String> list, String currentArgument) {
        for (World world : QuantumServer.get().getWorlds()) {
            NamespaceID id = world.getDimension().id();
            TabCompleting.addIfStartsWith(list, id, currentArgument);
        }
        return list;
    }

    public static List<String> worldIds(List<String> list, String currentArgument) {
        for (World world : QuantumServer.get().getWorlds()) {
            String uuid = world.getUID().toString();
            TabCompleting.addIfStartsWith(list, uuid, currentArgument);
        }
        return list;
    }

    public static List<String> strings(String currentArgument, String... strings) {
        return TabCompleting.strings(new ArrayList<>(), currentArgument, strings);
    }

    public static List<String> strings(String currentArgument, char... chars) {
        return TabCompleting.strings(new ArrayList<>(), currentArgument, TabCompleting.forceToString(chars));
    }

    public static List<String> strings(List<String> list, String currentArgument, String... strings) {
        for (var string : strings) {
            TabCompleting.addIfStartsWith(list, string, currentArgument);
        }
        return list;
    }

    public static List<String> strings(List<String> list, String currentArgument, char... chars) {
        for (var c : chars) {
            TabCompleting.addIfStartsWith(list, String.valueOf(c), currentArgument);
        }
        return list;
    }

    public static List<String> doubles(List<String> list, String currentArgument) {
        list.add(currentArgument);
        if (!currentArgument.isEmpty() && !currentArgument.contains(".")) {
            list.add(currentArgument + ".");
        }
        if (currentArgument.startsWith("0") && !currentArgument.startsWith("0.")) return list;
        for (var i = 0; i <= 9; i++) {
            list.add(currentArgument + i);
        }
        return list;
    }

    public static List<String> ints(List<String> list, String currentArgument) {
        if (!currentArgument.isEmpty()) {
            list.add(currentArgument);
        }
        if (currentArgument.startsWith("0")) return list;
        for (var i = 0; i <= 9; i++) {
            list.add(currentArgument + i);
        }
        return list;
    }

    public static List<String> hex(List<String> list, String currentArgument) {
        list.add(currentArgument);
        for (var c : "0123456789abcdef".toCharArray()) {
            list.add(currentArgument + c);
        }
        return list;
    }

    public static List<String> booleans(List<String> list, String currentArgument) {
        TabCompleting.addIfStartsWith(list, "true", currentArgument);
        TabCompleting.addIfStartsWith(list, "on", currentArgument);
        TabCompleting.addIfStartsWith(list, "yes", currentArgument);
        TabCompleting.addIfStartsWith(list, "enable", currentArgument);
        TabCompleting.addIfStartsWith(list, "false", currentArgument);
        TabCompleting.addIfStartsWith(list, "off", currentArgument);
        TabCompleting.addIfStartsWith(list, "no", currentArgument);
        TabCompleting.addIfStartsWith(list, "disable", currentArgument);
        return list;
    }

    public static List<String> mods(List<String> list, String currentArgument) {
        GamePlatform manager = GamePlatform.get();
        for (Mod plugin : manager.getMods()) {
            TabCompleting.addIfStartsWith(list, plugin.getName(), currentArgument);
        }
        return list;
    }

    public static List<String> commands(List<String> list, String currentArgument) {
        CommandRegistry.getCommandNames().forEach(s -> TabCompleting.addIfStartsWith(list, s, currentArgument));
        return list;
    }

    public static List<String> subCommand(List<String> list, CommandSender sender, String commandName, String... commandArgs) {
        Command command = CommandRegistry.get(commandName);
        if (command == null) return list;

        List<String> options = command.onTabComplete(sender, new CommandContext(commandName), commandName, commandArgs);
        if (options == null) return list;

        list.addAll(options);
        return list;
    }

    public static List<String> selectors(List<String> list, SelectorKey filterKey, String currentArgument, Collection<String> values) {
        return TabCompleting.selectors(list, filterKey, currentArgument, values.toArray(new String[]{}));
    }

    public static List<String> selectors(SelectorKey filterKey, String currentArgument, String... values) {
        return TabCompleting.selectors(new ArrayList<>(), filterKey, currentArgument, values);
    }

    public static List<String> selectors(SelectorKey filterKey, String currentArgument, Collection<String> values) {
        return TabCompleting.selectors(new ArrayList<>(), filterKey, currentArgument, values);
    }

    public static List<String> selectors(List<String> list, SelectorKey filterKey, String currentArgument, String... values) {
        if (currentArgument.isEmpty()) {
            TabCompleting.addIfStartsWith(list, filterKey.symbol(), currentArgument);
            return list;
        }
        for (var value : values) {
            TabCompleting.addIfStartsWith(list, filterKey.symbol() + value, currentArgument);
        }
        return list;
    }

    public static List<String> prefixed(List<String> list, String prefix, String currentArgument, Collection<String> values) {
        return TabCompleting.prefixed(list, prefix, currentArgument, values.toArray(new String[]{}));
    }

    public static List<String> prefixed(String prefix, String currentArgument, String... values) {
        return TabCompleting.prefixed(new ArrayList<>(), prefix, currentArgument, values);
    }

    public static List<String> prefixed(String prefix, String currentArgument, Collection<String> values) {
        return TabCompleting.prefixed(new ArrayList<>(), prefix, currentArgument, values);
    }

    public static List<String> prefixed(List<String> list, String prefix, String currentArgument, String... values) {
        if (currentArgument.isEmpty()) {
            TabCompleting.addIfStartsWith(list, prefix, currentArgument);
            return list;
        }
        for (var value : values) {
            TabCompleting.addIfStartsWith(list, prefix + value, currentArgument);
        }
        return list;
    }

    public static List<String> numbers(String currentArgument, Number... numbers) {
        return TabCompleting.numbers(new ArrayList<>(), currentArgument, numbers);
    }

    public static List<String> numbers(List<String> list, String currentArgument, Number... numbers) {
        return TabCompleting.numbers(list, currentArgument, 10, numbers);
    }

    public static List<String> numbers(List<String> list, String currentArgument, int radix, Number... numbers) {
        for (var number : numbers) {
            if (number.toString().contains(currentArgument)) {
                list.add(Long.toUnsignedString(number.longValue(), radix));
            }
        }
        return list;
    }

    public static List<String> variables(ArrayList<Object> objects, String s, ServerPlayer player, Class<?> clazz) {
        List<String> list = PlayerVariables.get(player).getVariablesByType(clazz).collect(Collectors.toList());
        for (var variable : list) {
            if (variable.startsWith(s)) {
                objects.add(variable);
            }
        }

        return list;
    }

    public static String[] forceToString(char... chars) {
        return (String[]) Stream.of(chars).map(String::valueOf).toArray();
    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull String text, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(text, "text");
        Preconditions.checkNotNull(startsWith, "startsWith");
        if (text.startsWith(startsWith)) {
            list.add(text);
        }

    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull UUID uuid, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(uuid, "uuid");
        Preconditions.checkNotNull(startsWith, "startsWith");

        var text = uuid.toString();
        if (text.toLowerCase(Locale.getDefault()).startsWith(startsWith)) {
            list.add(text);
        }
    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull NamespaceID id, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkNotNull(startsWith, "startsWith");

        if (id.getPath().startsWith(startsWith)) {
            list.add(id.toString());
        } else {
            if (id.toString().startsWith(startsWith)) {
                list.add(id.toString());
            }
        }
    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull Object obj, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(obj, "obj");
        Preconditions.checkNotNull(startsWith, "startsWith");
        var text = obj.toString();
        if (!text.contains(" ") && text.startsWith(startsWith)) {
            list.add(text);
        }
    }

    public static List<String> entityIds(List<String> list, ServerWorld world, String currentArgument) {
        for (var id : Arrays.stream(world.getEntities().toArray(Entity.class)).map(Entity::getId).toList()) {
            TabCompleting.addIfStartsWith(list, id, currentArgument);
        }

        return list;
    }
}