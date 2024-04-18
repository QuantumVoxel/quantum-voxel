package com.ultreon.quantum.client.player;

import com.ultreon.quantum.network.packets.AddPermissionPacket;
import com.ultreon.quantum.network.packets.InitialPermissionsPacket;
import com.ultreon.quantum.network.packets.RemovePermissionPacket;
import com.ultreon.quantum.server.player.PermissionMap;

public class ClientPermissionMap extends PermissionMap {
    public void onPacket(AddPermissionPacket packet) {
        this.allows.add(packet.getPermission());
    }

    public void onPacket(RemovePermissionPacket packet) {
        this.allows.remove(packet.getPermission());
    }

    public void onPacket(InitialPermissionsPacket packet) {
        this.allows.addAll(packet.getPermissions());
    }
}
