package com.ultreon.craft.network.partial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.Arrays;
import java.util.List;

public class PacketSplitter extends MessageToMessageEncoder<PacketBufferInfo> {

    /**
     * Splits the packet buffer into multiple partial packets.
     * This is used to split large packets into multiple smaller ones to reduce packet lag.
     *
     * @param ctx the channel handler context
     * @param packetBuffer the packet buffer to split
     * @param list the list to add the partial packets to
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, PacketBufferInfo packetBuffer, List<Object> list) {
        PartialPacket[] split = packetBuffer.buffer().split(packetBuffer.packetId(), packetBuffer.sequence());
        list.addAll(Arrays.asList(split));
    }
}
