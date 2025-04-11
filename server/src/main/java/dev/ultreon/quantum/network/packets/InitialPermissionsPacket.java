package dev.ultreon.quantum.network.packets;

import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;

import java.util.List;

public class InitialPermissionsPacket implements Packet<InGameClientPacketHandler> {
    private final List<Permission> permissions;

    public InitialPermissionsPacket(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public InitialPermissionsPacket(PacketIO buffer) {
        this.permissions = buffer.readList((buf) -> new Permission(buffer.readString(128)));
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeList(this.permissions, (buf, permission) -> buffer.writeString(this.permissions.toString(), 128));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onInitialPermissions(this);
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }
}
