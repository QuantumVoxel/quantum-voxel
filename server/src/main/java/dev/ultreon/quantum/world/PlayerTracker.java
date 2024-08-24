package dev.ultreon.quantum.world;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerTracker implements Audience {
    private final Set<ServerPlayer> players = new HashSet<>();

    public void startTracking(ServerPlayer player) {
        this.players.add(player);
    }

    public void stopTracking(ServerPlayer player) {
        this.players.remove(player);
    }

    public boolean isTracking(ServerPlayer player) {
        return this.players.contains(player);
    }

    public boolean isAnyoneTracking() {
        return !this.players.isEmpty();
    }

    public boolean isNobodyTracking() {
        return this.players.isEmpty();
    }

    @Override
    public void sendPacket(Packet<? extends ClientPacketHandler> packet) {
        for (var player : this.players)
             player.connection.send(packet);
    }

    @Override
    public void sendMessage(String message) {
        for (var player : this.players)
            player.sendMessage(message);
    }

    @Override
    public void sendMessage(TextObject message) {
        for (var player : this.players)
            player.sendMessage(message);
    }
}
