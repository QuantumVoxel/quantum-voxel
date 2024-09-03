package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.GameMode;

public record S2CGamemodePacket(GameMode gameMode) implements Packet<InGameClientPacketHandler> {

    public static S2CGamemodePacket read(PacketIO buffer) {
        var gameMode = GameMode.byOrdinal(buffer.readByte());

        return new S2CGamemodePacket(gameMode);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(this.gameMode.ordinal());
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onGamemode(this.gameMode);
    }

    @Override
    public String toString() {
        return "S2CGamemodePacket{" +
               "gameMode=" + gameMode +
               '}';
    }
}
