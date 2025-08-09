package dev.ultreon.quantum.network.stage;

import dev.ultreon.quantum.network.packets.c2s.*;
import dev.ultreon.quantum.network.packets.s2c.*;

public class LoginPacketStage extends PacketStage {
    protected LoginPacketStage() {
        super();
    }

    @Override
    public void registerPackets() {
        super.registerPackets();

        this.addServerBound(C2SLoginPacket::read);
        this.addClientBound(S2CLoginAcceptedPacket::read);
    }
}
