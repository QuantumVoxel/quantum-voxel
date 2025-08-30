package dev.ultreon.quantum.api.commands.variables;

import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.api.events.ServerPlayerEvent;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.util.Result;

import java.util.*;

public class PlayerVariables {
    private static final Map<UUID, PlayerVariables> players = new HashMap<>();

    static {
        EventSystem.addListenerDefault(ServerPlayerEvent.Left.class, event -> players.remove(event.getEntity().getUuid()));
    }

    private final Map<String, Object> variables = new HashMap<>();

    private final ServerPlayer player;

    private PlayerVariables(ServerPlayer player) {
        this.player = player;
    }

    public static PlayerVariables get(ServerPlayer player) {
        return players.computeIfAbsent(player.getUuid(), uuid -> new PlayerVariables(player));
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public <T> Result<T> getVariable(String name, Class<T> clazz) {
        Object variable = getVariable(name);
        if (variable == null) return Result.ok(null);
        if (clazz.isInstance(variable)) return Result.ok(clazz.cast(variable));
        return Result.failure(new ClassCastException("Variable " + name + " is not of type " + clazz.getName()));
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Set<String> getVariablesByType(Class<?> clazz) {
        Set<Map.Entry<String, Object>> entries = variables.entrySet();
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, Object> entry : entries) {
            if (clazz.isInstance(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }
}
