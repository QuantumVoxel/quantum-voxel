package com.ultreon.quantum.network.partial;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PartialPacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        list.add(new PartialPacket(byteBuf.readMedium(), byteBuf.readLong(), byteBuf.readInt(), byteBuf.readInt(), byteBuf.readBytes(byteBuf.readInt())));
    }
}
