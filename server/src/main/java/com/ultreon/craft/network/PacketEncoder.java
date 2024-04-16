package com.ultreon.craft.network;

import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.network.partial.PacketBufferInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.AttributeKey;

import java.util.List;

public class PacketEncoder extends MessageToMessageEncoder<PacketSequence<?>> {
    public static final Pool<Long> sequencePool = new Pool<>() {
        private long next = 0;

        @Override
        protected Long newObject() {
            return next++;
        }
    };
    private final AttributeKey<? extends PacketData<?>> ourDataKey;

    public PacketEncoder(AttributeKey<? extends PacketData<?>> ourDataKey) {
        this.ourDataKey = ourDataKey;
    }

    public static void handleReply(long sequenceId) {
        sequencePool.free(sequenceId);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, PacketSequence<?> msg, List<Object> list) {
        PacketBuffer buffer = new PacketBuffer();
        PacketData<?> data = ctx.channel().attr(this.ourDataKey).get();
        buffer.writeInt((data).getId(msg.packet()));
        data.encode(msg.packet(), buffer);

        list.add(new PacketBufferInfo(data.getId(msg.packet()), msg.sequence(), buffer));
    }
}
