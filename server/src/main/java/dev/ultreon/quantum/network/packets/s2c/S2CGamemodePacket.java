package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.util.GameMode;

public class S2CGamemodePacket extends Packet<InGameClientPacketHandler> {
    private final GameMode gameMode;

    public S2CGamemodePacket(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public S2CGamemodePacket(PacketIO buffer) {
        this.gameMode = GameMode.byOrdinal(buffer.readByte());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(this.gameMode.ordinal());
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onGamemode(this.gameMode);
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    @Override
    public String toString() {
        return "S2CGamemodePacket{" +
                "gameMode=" + gameMode +
                '}';
    }
}
