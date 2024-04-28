package com.ultreon.quantum.network.packets.s2c;

import com.ultreon.quantum.network.PacketIO;
import com.ultreon.quantum.network.PacketContext;
import com.ultreon.quantum.network.client.InGameClientPacketHandler;
import com.ultreon.quantum.network.packets.Packet;
import com.ultreon.quantum.util.GameMode;

public class S2CGamemodePacket extends Packet<InGameClientPacketHandler> {
    private final GameMode gamemode;

    public S2CGamemodePacket(GameMode gamemode) {
        this.gamemode = gamemode;
    }

    public S2CGamemodePacket(PacketIO buffer) {
        this.gamemode = GameMode.byOrdinal(buffer.readByte());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeByte(this.gamemode.ordinal());
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onGamemode(this.gamemode);
    }
}
