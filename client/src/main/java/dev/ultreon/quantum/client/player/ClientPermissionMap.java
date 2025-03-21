package dev.ultreon.quantum.client.player;

import dev.ultreon.quantum.network.packets.AddPermissionPacket;
import dev.ultreon.quantum.network.packets.InitialPermissionsPacket;
import dev.ultreon.quantum.network.packets.RemovePermissionPacket;
import dev.ultreon.quantum.server.player.PermissionMap;

/**
 * Represents the permission map of the client.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ClientPermissionMap extends PermissionMap {
    /**
     * Handles the {@link AddPermissionPacket} packet.
     *
     * @param packet the packet to handle.
     */
    public void onPacket(AddPermissionPacket packet) {
        this.allows.add(packet.getPermission());
    }

    /**
     * Handles the {@link RemovePermissionPacket} packet.
     *
     * @param packet the packet to handle.
     */
    public void onPacket(RemovePermissionPacket packet) {
        this.allows.remove(packet.getPermission());
    }

    /**
     * Handles the {@link InitialPermissionsPacket} packet.
     *
     * @param packet the packet to handle.
     */
    public void onPacket(InitialPermissionsPacket packet) {
        this.allows.addAll(packet.getPermissions());
    }
}
