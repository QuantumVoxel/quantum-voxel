package com.ultreon.quantum.client.network;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.api.events.ClientPlayerEvents;
import com.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import com.ultreon.quantum.client.player.LocalPlayer;
import com.ultreon.quantum.client.rpc.GameActivity;
import com.ultreon.quantum.client.world.ClientWorld;
import com.ultreon.quantum.client.world.WorldRenderer;
import com.ultreon.quantum.entity.EntityTypes;
import com.ultreon.quantum.network.client.ClientPacketHandler;
import com.ultreon.quantum.network.server.ServerPacketHandler;
import com.ultreon.quantum.network.stage.PacketStages;
import com.ultreon.quantum.network.system.IConnection;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.LoginClientPacketHandler;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.UUID;

public class LoginClientPacketHandlerImpl implements LoginClientPacketHandler {
    private final QuantumClient client = QuantumClient.get();
    private final IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    private boolean disconnected;

    public LoginClientPacketHandlerImpl(IConnection<ClientPacketHandler, ServerPacketHandler> connection) {
        this.connection = connection;
    }

    @Override
    public void onLoginAccepted(UUID uuid) {
        this.client.connection.moveTo(PacketStages.IN_GAME, new InGameClientPacketHandlerImpl(this.connection));

        ClientWorld clientWorld = new ClientWorld(this.client);
        this.client.world = clientWorld;
        this.client.worldRenderer = new WorldRenderer(this.client.world);
        this.client.renderWorld = true;
        this.client.inspection.createNode("world", () -> this.client.world);

        var player = this.client.player = new LocalPlayer(EntityTypes.PLAYER, clientWorld, uuid);
        clientWorld.spawn(player);

        IConnection.LOGGER.info("Logged in with uuid: {}", uuid);

        ClientPlayerEvents.PLAYER_JOINED.factory().onPlayerJoined(player);

        if (this.client.integratedServer != null) this.client.setActivity(GameActivity.SINGLEPLAYER);
        else this.client.setActivity(GameActivity.MULTIPLAYER);
    }

    @Override
    public void onDisconnect(String message) {
        this.disconnected = true;
        try {
            this.connection.close();
        } catch (ClosedConnectionException | ClosedChannelException e) {
            // Ignored
        } catch (IOException e) {
            IConnection.LOGGER.error("Failed to close connection", e);
        }

        this.client.showScreen(new DisconnectedScreen(message, !this.connection.isMemoryConnection()));
    }

    @Override
    public boolean isAcceptingPackets() {
        return false;
    }

    @Override
    public PacketContext context() {
        return null;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}
