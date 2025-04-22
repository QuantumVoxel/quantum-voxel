package dev.ultreon.quantum.client.rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.Packet;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.User;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import dev.ultreon.quantum.Promise;

/**
 * A class that handles Discord Rich Presence integration. The Discord IPC client is used to send
 * Rich Presence updates to Discord.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class DiscordRPC implements RpcHandler {
    /**
     * The current game activity.
     */
    private GameActivity activity;

    /**
     * A flag that indicates whether the Rich Presence has been updated since the last update.
     */
    private volatile boolean updated;

    /**
     * The Discord IPC client.
     */
    private IPCClient client;

    /**
     * Starts the DiscordRPC class, which initializes the Discord IPC client and sets up the IPC listener
     * for the client.
     */
    @Override
    public void start() {
        // Initialize the Discord IPC client with the application ID.
        client = new IPCClient(1179401719902384138L);

        // Sets up the IPC listener for the Discord IPC client. This method is called when the client is ready to start
        // sending Rich Presence updates to Discord.
        client.setListener(new IPCListener() {
            /**
             * Event handler for when the IPC client is ready to send Rich Presence updates. Sets up a scheduled
             * task to update the Rich Presence every 200 milliseconds. Additionally, sets up a shutdown hook to
             * cancel the scheduled task and close the IPC client when the client is about to shut down.
             *
             * @param client the IPC client
             */
            @Override
            public void onReady(IPCClient client) {
                CommonConstants.LOGGER.info("Discord RPC is ready!");

                // Create a Rich Presence builder and set the initial state and details
                RichPresence.Builder builder = new RichPresence.Builder();
                QuantumClient quantumClient = QuantumClient.get();
                if (activity == null) { // Loading
                    builder.setState("Loading...")
                            .setDetails("Version: " + QuantumClient.getGameVersion())
                            .setStartTimestamp(OffsetDateTime.now());
                } else if (activity == GameActivity.SINGLEPLAYER) {
                    @Nullable ClientWorldAccess world = quantumClient.world;
                    builder.setState("Playing Singleplayer")
                            .setDetails("In: " + (world != null ? world.getDimension().id() : null))
                            .setStartTimestamp(OffsetDateTime.now())
                            .setLargeImage("icon", "Version: " + QuantumClient.getGameVersion());
                } else if (activity == GameActivity.MULTIPLAYER) {
                    @Nullable ClientWorldAccess world = quantumClient.world;
                    builder.setState("Playing Multiplayer")
                            .setDetails("In: " + (world != null ? world.getDimension().id() : null))
                            .setStartTimestamp(OffsetDateTime.now())
                            .setLargeImage("icon", "Version: " + QuantumClient.getGameVersion())
                            .setJoinSecret(quantumClient.serverInfo.address());
                }

                builder.setJoinSecret("hello_world");

                // Send the initial Rich Presence
                client.sendRichPresence(builder.build());

                // Schedule a task to update the Rich Presence every 200 milliseconds
                // TODO : This is not working
//                ScheduledFuture<?> scheduledFuture = QuantumClient.get().scheduleRepeat(() -> update(client), 0, 200, TimeUnit.MILLISECONDS);

                // Set up a shutdown hook to cancel the scheduled task and close the IPC client
//                Runtime.getRuntime().addShutdownHook(new Thread(() -> scheduledFuture.cancel(false)));
            }
            /**
             * Updates the Discord Rich Presence with the current game activity.
             *
             * @param client The IPC client used to send the Rich Presence.
             */
            private void update(IPCClient client) {
                // Only update if the Rich Presence has been updated since the last update.
                if (!updated) {
                    return;
                }

                // Update flag to false to prevent multiple updates.
                updated = false;

                RichPresence.Builder builder = new RichPresence.Builder();

                // Determine the game activity and set the appropriate Rich Presence details.
                if (activity == GameActivity.SINGLEPLAYER) {
                    @Nullable ClientWorldAccess world = QuantumClient.get().world;
                    builder.setState("Playing Singleplayer")
                            .setDetails("In: " + (world != null ? world.getDimension().id() : null))
                            .setStartTimestamp(OffsetDateTime.now())
                            .setLargeImage("icon", "Version: " + QuantumClient.getGameVersion());
                } else if (activity == GameActivity.MULTIPLAYER) {
                    @Nullable ClientWorldAccess world = QuantumClient.get().world;
                    builder.setState("Playing Multiplayer")
                            .setDetails("In: " + (world != null ? world.getDimension().id() : null))
                            .setStartTimestamp(OffsetDateTime.now())
                            .setLargeImage("icon", "Version: " + QuantumClient.getGameVersion())
                            .setJoinSecret(QuantumClient.get().serverInfo.address());
                }

                // Send the updated Rich Presence to the Discord client.
                client.sendRichPresence(builder.build());

            }
            /**
             * Called when a user requests to join the game.
             *
             * @param client The IPC client.
             * @param secret The secret to connect to the server.
             * @param user The user who requested the join.
             */
            @Override
            public void onActivityJoinRequest(IPCClient client, String secret, User user) {
                if (user.isBot()) {
                    // This should never happen, but still funny to add!
                    QuantumClient.get().notifications.add("Discord RPC", "Beep boop, a robot requested to join!", "discordrpc");
                    return;
                }

                // Add notification with user's name and discriminator
                String name = user.getName();
                String discriminator = user.getDiscriminator();
                String notificationMessage = "Join request received from " + name + "#" + discriminator + "!";
                QuantumClient.get().notifications.add("Discord RPC", notificationMessage, "discordrpc");
            }
            /**
             * Called when a user has accepted the join request and this client should connect to the server.
             *
             * @param client The IPC client.
             * @param secret The secret to connect to the server.
             */
            @Override
            public void onActivityJoin(IPCClient client, String secret) {
                IPCListener.super.onActivityJoin(client, secret);

                // Check if the secret is valid
                if (!secret.matches("[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+):\\d+")) {
                    QuantumClient.get().notifications.add("Discord RPC", "Invalid join request!", "discordrpc");
                    return;
                }

                // Split the secret into server address and port
                String[] split = secret.split(":");

                // Connect to the server
                QuantumClient.get().connectToServer(split[0], Integer.parseInt(split[1]));
            }

            /**
             * Called when the client is disconnected from Discord.
             *
             * @param client The IPC client.
             * @param t      The cause of the disconnection. Maybe null.
             */
            @Override
            public void onDisconnect(IPCClient client, Throwable t) {
                if (QuantumClient.get().isShutdown()) return;
                QuantumClient.get().notifications.add("Discord RPC", "Disconnected from Discord!", "discordrpc");
            }

            /**
             * Called when a spectate request is received.
             *
             * @param client The IPC client.
             * @param secret The secret of the spectate request.
             */
            @Override
            public void onActivitySpectate(IPCClient client, String secret) {
                QuantumClient.get().notifications.add("Discord RPC", "Spectate request received, wait what?", "discordrpc");
            }

            /**
             * Called when a packet is received.
             *
             * @param client The IPC client.
             * @param packet The received packet.
             */
            @Override
            public void onPacketReceived(IPCClient client, Packet packet) {
                IPCListener.super.onPacketReceived(client, packet);

                if (packet.getOp() == Packet.OpCode.CLOSE) {
                    CommonConstants.LOGGER.info("Discord RPC disconnected!");
                    client.close();
                }
            }
        });

        Promise.runAsync(() -> {
            CommonConstants.LOGGER.info("Attempting to connect to Discord...");
            try {
                client.connect();
            } catch (NoDiscordClientException e) {
                QuantumClient.get().notifications.add("Discord RPC", "Unable to connect to Discord!", "discordrpc");
                CommonConstants.LOGGER.error("Unable to connect to Discord", e);
                try {
                    client.close();
                } catch (Throwable ignored) {
                    // Ignored
                }
            } catch (Throwable e) {
                CommonConstants.LOGGER.error("Unable to connect to Discord", e);
                try {
                    client.close();
                } catch (Throwable ignored) {
                    // Ignored
                }
            }
        });
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (Throwable ignored) {
            // Ignored
        }
    }

    @Override
    public void setActivity(GameActivity newActivity) {
        this.activity = newActivity;
        this.updated = true;
    }
}
