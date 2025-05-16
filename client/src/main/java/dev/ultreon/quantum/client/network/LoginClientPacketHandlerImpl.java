package dev.ultreon.quantum.client.network;

import com.sun.jdi.connect.spi.ClosedConnectionException;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientPlayerEvents;
import dev.ultreon.quantum.client.gui.screens.DisconnectedScreen;
import dev.ultreon.quantum.client.gui.screens.WorldLoadScreen;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.rpc.GameActivity;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.entity.EntityTypes;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.client.LoginClientPacketHandler;
import dev.ultreon.quantum.network.packets.s2c.S2CLoginAcceptedPacket;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.stage.PacketStages;
import dev.ultreon.quantum.network.system.IConnection;
import dev.ultreon.quantum.registry.S2CRegistrySync;
import dev.ultreon.quantum.server.S2CRegistriesSync;
import dev.ultreon.quantum.util.GameMode;
import dev.ultreon.quantum.util.Vec3d;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.UUID;

public class LoginClientPacketHandlerImpl implements LoginClientPacketHandler {
    private final QuantumClient client = QuantumClient.get();
    private final IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    private boolean disconnected;

    public LoginClientPacketHandlerImpl(IConnection<ClientPacketHandler, ServerPacketHandler> connection) {
        this.connection = connection;
    }

    @Override
    public void onLoginAccepted(S2CLoginAcceptedPacket packet) {

        UUID uuid = packet.uuid();
        Vec3d spawnPos = packet.spawnPos();
        GameMode gameMode = packet.gameMode();
        float health = packet.health();
        int hunger = packet.hunger(); // TODO

        this.client.connection.moveTo(PacketStages.IN_GAME, new InGameClientPacketHandlerImpl(this.connection));

        var player = this.client.player = new LocalPlayer(EntityTypes.PLAYER, this.client.world, uuid);
        ClientPlayerEvents.PLAYER_JOINED.factory().onPlayerJoined(player);

        player.setPosition(spawnPos.x, spawnPos.y, spawnPos.z);
        player.setHealth(health);
        player.getFoodStatus().setFoodLevel(hunger);
        player.setGameMode(gameMode);

        Objects.requireNonNull(this.client.world).spawn(player);

        IConnection.LOGGER.info("Logged in with uuid: {}", uuid);

        if (this.client.integratedServer != null) this.client.setActivity(GameActivity.SINGLEPLAYER);
        else this.client.setActivity(GameActivity.MULTIPLAYER);

        if (this.client.screen instanceof WorldLoadScreen) {
            WorldLoadScreen worldLoadScreen = (WorldLoadScreen) this.client.screen;
            QuantumClient.invoke(worldLoadScreen::onLogin);
        } else {
            QuantumClient.invoke(() -> {
                this.client.renderWorld = true;
                ClientWorld clientWorldAccess = this.client.world;
                if (clientWorldAccess instanceof ClientWorld) {
                    ClientWorld clientWorld = clientWorldAccess;
                    this.client.worldRenderer = new WorldRenderer(clientWorld);
                }
                this.client.showScreen(null);
            });
        }
    }

    @Override
    public void onRegistrySync(S2CRegistrySync packet) {
        this.client.registries.load(packet.getRegistryID(), packet.getRegistryMap());
    }

    @Override
    public void onRegistriesSync(S2CRegistriesSync packet) {
        this.client.registries.load(packet.getRegistries());
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
