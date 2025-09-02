package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.GameMode;

import java.util.Objects;

public final class S2CGamemodePacket implements Packet<InGameClientPacketHandler> {
    private final GameMode gameMode;

    public S2CGamemodePacket(GameMode gameMode) {
        this.gameMode = gameMode;
    }

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

    public GameMode gameMode() {
        return gameMode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (S2CGamemodePacket) obj;
        return Objects.equals(this.gameMode, that.gameMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameMode);
    }

}
