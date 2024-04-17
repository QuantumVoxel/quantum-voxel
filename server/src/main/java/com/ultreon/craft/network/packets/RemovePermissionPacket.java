package com.ultreon.craft.network.packets;

import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.network.PacketIO;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;

public class RemovePermissionPacket extends Packet<InGameClientPacketHandler> {
    private final Permission permission;

    public RemovePermissionPacket(Permission permission) {
        this.permission = permission;
    }

    public RemovePermissionPacket(PacketIO buffer) {
        this.permission = new Permission(buffer.readString(128));
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeUTF(this.permission.toString(), 128);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRemovePermission(this);
    }

    public Permission getPermission() {
        return this.permission;
    }
}
