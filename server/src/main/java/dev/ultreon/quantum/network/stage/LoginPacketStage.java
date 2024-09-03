package dev.ultreon.quantum.network.stage;

import dev.ultreon.quantum.network.packets.c2s.C2SDisconnectPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CDisconnectPacket;
import dev.ultreon.quantum.network.packets.s2c.S2CLoginAcceptedPacket;

public class LoginPacketStage extends PacketStage {
    protected LoginPacketStage() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerPackets() {
        this.addServerBound(C2SDisconnectPacket::read);
        this.addClientBound(S2CDisconnectPacket::read);

        this.addServerBound(C2SLoginPacket::read);
        this.addClientBound(S2CLoginAcceptedPacket::read);
    }
}
