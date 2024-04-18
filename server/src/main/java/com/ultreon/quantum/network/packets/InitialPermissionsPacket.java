package com.ultreon.quantum.network.packets;

import com.ultreon.quantum.api.commands.perms.Permission;
import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;

import java.util.List;

public class InitialPermissionsPacket extends Packet<InGameClientPacketHandler> {
    private final List<Permission> permissions;

    public InitialPermissionsPacket(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public InitialPermissionsPacket(PacketIO buffer) {
        this.permissions = buffer.readList((buf) -> new Permission(buffer.readString(128)));
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeList(this.permissions, (buf, permission) -> buffer.writeUTF(this.permissions.toString(), 128));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onInitialPermissions(this);
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }
}
