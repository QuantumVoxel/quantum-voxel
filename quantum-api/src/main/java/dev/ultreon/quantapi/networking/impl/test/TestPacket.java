package dev.ultreon.quantapi.networking.impl.test;

import dev.ultreon.quantapi.networking.api.packet.PacketToClient;
import dev.ultreon.quantapi.QuantAPI;
import dev.ultreon.quantum.network.PacketIO;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TestPacket extends PacketToClient<TestPacket> {
    public TestPacket() {

    }

    public TestPacket(PacketIO buffer) {

    }

    @Override
    protected void handle() {
        QuantAPI.LOGGER.info("Successfully received!");
    }

    @Override
    public void toBytes(PacketIO buffer) {

    }
}
