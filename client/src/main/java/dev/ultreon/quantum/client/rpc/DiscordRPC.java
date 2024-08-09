package dev.ultreon.quantum.client.rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public class DiscordRPC implements RpcHandler {
    private GameActivity activity;

    @Override
    public void start() {
        IPCClient client = new IPCClient(1179401719902384138L);
        client.setListener(new IPCListener() {
            @Override
            public void onReady(IPCClient client) {
                CommonConstants.LOGGER.info("Discord RPC is ready!");

                RichPresence.Builder builder = new RichPresence.Builder();
                QuantumClient quantumClient = QuantumClient.get();
                if (activity == null) { // Loading
                    builder.setState("Loading...")
                            .setDetails("Version: " + QuantumClient.getGameVersion())
                            .setStartTimestamp(OffsetDateTime.now());
                } else if (activity == GameActivity.SINGLEPLAYER) {
                    @Nullable ClientWorldAccess world = quantumClient.world;
                    builder.setState("Playing Singleplayer")
                            .setDetails("In: " + (world != null ? world.getDimension().getName().getText() : null))
                            .setStartTimestamp(OffsetDateTime.now())
                            .setLargeImage("icon", "Version: " + QuantumClient.getGameVersion());
                } else if (activity == GameActivity.MULTIPLAYER) {
                    @Nullable ClientWorldAccess world = quantumClient.world;
                    builder.setState("Playing Multiplayer")
                            .setDetails("In: " + (world != null ? world.getDimension().getName().getText() : null))
                            .setStartTimestamp(OffsetDateTime.now())
                            .setLargeImage("icon", "Version: " + QuantumClient.getGameVersion())
                            .setJoinSecret(quantumClient.serverData.address());
                }
                client.sendRichPresence(builder.build());
            }

            @Override
            public void onActivityJoinRequest(IPCClient client, String secret, User user) {
                if (user.isBot()) {
                    // This should never happen, but still funny to add!
                    QuantumClient.get().notifications.add("Discord RPC", "Beep boop, a robot requested to join!", "discordrpc");
                    return;
                }

                QuantumClient.get().notifications.add("Discord RPC", "Join request received from " + user.getName() + "#" + user.getDiscriminator() + "!", "discordrpc");
            }

            @Override
            public void onActivityJoin(IPCClient client, String secret) {
                IPCListener.super.onActivityJoin(client, secret);

                if (!secret.matches("[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+):\\d+")) {
                    QuantumClient.get().notifications.add("Discord RPC", "Invalid join request!", "discordrpc");
                    return;
                }
                String[] split = secret.split(":");
                QuantumClient.get().connectToServer(split[0], Integer.parseInt(split[1]));
            }

            @Override
            public void onDisconnect(IPCClient client, Throwable t) {
                QuantumClient.get().notifications.add("Discord RPC", "Disconnected from Discord!", "discordrpc");
            }

            @Override
            public void onActivitySpectate(IPCClient client, String secret) {
                QuantumClient.get().notifications.add("Discord RPC", "Spectate request received, wait what?", "discordrpc");
            }

            @Override
            public void onPacketReceived(IPCClient client, Packet packet) {
                IPCListener.super.onPacketReceived(client, packet);

                if (packet.getOp() == Packet.OpCode.CLOSE) {
                    CommonConstants.LOGGER.info("Discord RPC disconnected!");
                    client.close();
                }
            }
        });
        try {
            client.connect();
        } catch (NoDiscordClientException e) {
            QuantumClient.get().notifications.add("Discord RPC", "Unable to connect to Discord!", "discordrpc");
            CommonConstants.LOGGER.error("Unable to connect to Discord", e);
        } catch (Throwable e) {
            QuantumClient.get().notifications.add("Discord RPC ERROR", e.getMessage(), "discordrpc");
            CommonConstants.LOGGER.error("Unable to connect to Discord", e);
        }

    }

    @Override
    public void setActivity(GameActivity newActivity) {
        this.activity = newActivity;
    }
}
