package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;

public class RemovePermissionPacket implements Packet<InGameClientPacketHandler> {
    private final Permission permission;

    public RemovePermissionPacket(Permission permission) {
        this.permission = permission;
    }

    public RemovePermissionPacket(PacketIO buffer) {
        this.permission = new Permission(buffer.readString(128));
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeString(this.permission.toString(), 128);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemovePermission(this);
    }

    public Permission getPermission() {
        return this.permission;
    }
}
