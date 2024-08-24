package dev.ultreon.quantapi.networking.impl.test;

import dev.ultreon.quantapi.networking.api.IPacketRegisterContext;
import dev.ultreon.quantapi.networking.impl.Network;
import dev.ultreon.quantapi.networking.impl.UcNetworkingMod;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class TestNetwork extends Network {
    private static final String CHANNEL_NAME = "test_channel";

    public TestNetwork() {
        super(UcNetworkingMod.MOD_ID, TestNetwork.CHANNEL_NAME);
    }

    @Override
    protected void registerPackets(IPacketRegisterContext ctx) {
        ctx.register(TestPacket::new);
    }
}
