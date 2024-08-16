package dev.ultreon.quantum.world;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;

import java.util.ArrayList;
import java.util.List;

public class PlayerTracker implements Audience {
    private final List<ServerPlayer> players = new ArrayList<>();

    public void startTracking(ServerPlayer player) {
        this.players.add(player);
        CommonConstants.LOGGER.info("Tracking player '%s'...", player.getName());
    }

    public void stopTracking(ServerPlayer player) {
        this.players.remove(player);
        CommonConstants.LOGGER.info("Stopped tracking player '%s'...", player.getName());
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
