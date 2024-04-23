package com.ultreon.quantum.api.commands.variables;

import com.ultreon.quantum.events.PlayerEvents;
import com.ultreon.quantum.server.player.ServerPlayer;
import com.ultreon.quantum.util.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class PlayerVariables {
    private static final Map<UUID, PlayerVariables> players = new HashMap<>();

    static {
        PlayerEvents.PLAYER_LEFT.subscribe(player1 -> players.remove(player1.getUuid()));
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

    public Stream<String> getVariablesByType(Class<?> clazz) {
        return variables.entrySet().stream().filter(entry -> clazz.isInstance(entry.getValue())).map(Map.Entry::getKey);
    }
}
