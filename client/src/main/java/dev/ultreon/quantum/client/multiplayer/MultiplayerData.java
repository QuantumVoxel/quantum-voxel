package dev.ultreon.quantum.client.multiplayer;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.RemotePlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MultiplayerData {
    private final Map<UUID, RemotePlayer> remotePlayers = new HashMap<>();
    private final QuantumClient client;

    public MultiplayerData(QuantumClient client) {
        this.client = client;
    }

    public RemotePlayer getRemotePlayerByUuid(UUID uuid) {
        return this.remotePlayers.get(uuid);
    }

    public Collection<RemotePlayer> getRemotePlayers() {
        return this.remotePlayers.values();
    }

    public RemotePlayer getRemotePlayerByName(String name) {
        for (RemotePlayer player : this.remotePlayers.values()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    @CanIgnoreReturnValue
    public RemotePlayer addPlayer(UUID uuid, String name, Vec3d position) {
        QuantumClient.LOGGER.info("%s joined the server.", name);
        RemotePlayer player = new RemotePlayer(this.client.world);
        player.setUuid(uuid);
        player.setName(name);
        player.setPosition(position);
        this.remotePlayers.put(uuid, player);
        return player;
    }

    public void removePlayer(UUID uuid) {
        RemotePlayer remotePlayer = this.remotePlayers.get(uuid);
        if (remotePlayer == null) return;

        QuantumClient.LOGGER.info("%s left the server.", remotePlayer.getName());
        this.remotePlayers.remove(uuid);
    }

    public void clear() {
        this.remotePlayers.clear();
    }
}
