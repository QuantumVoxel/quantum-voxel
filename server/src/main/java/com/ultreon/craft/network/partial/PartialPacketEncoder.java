package com.ultreon.craft.network.partial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PartialPacketEncoder extends MessageToByteEncoder<PartialPacket> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PartialPacket partialPacket, ByteBuf byteBuf) {
        byteBuf.writeMedium(partialPacket.packetId());
        byteBuf.writeLong(partialPacket.sequenceId());
        byteBuf.writeInt(partialPacket.dataOffset());
        byteBuf.writeInt(partialPacket.dataLength());
        byteBuf.writeBytes(partialPacket.data());
    }
}
