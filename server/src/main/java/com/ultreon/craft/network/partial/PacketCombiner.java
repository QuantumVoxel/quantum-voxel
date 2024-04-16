package com.ultreon.craft.network.partial;

import com.badlogic.gdx.utils.LongMap;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketIntegrityException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Combines {@link PartialPacket}s into a single {@link PacketBuffer}
 *
 * @author XyperCode
 * @since 0.1.0
 */
public class PacketCombiner extends MessageToMessageDecoder<PartialPacket> {
    private final LongMap<PartialMergeData> parts = new LongMap<>();

    /**
     * Decodes an array of partial packets into a single {@link PacketBuffer}
     * <p>
     * Note: if the partial packets are incorrectly ordered or is missing regions, this will throw a {@link PacketIntegrityException}.
     *
     * @param ctx the channel handler context
     * @param part array of partial packets
     * @param list a singleton list of a {@link PacketBuffer}
     * @throws DecoderException if the packet integrity check fails (see {@link PacketBuffer#validate(List)}).
     *                          Or if a generic decoding error occurs.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, PartialPacket part, List<Object> list) throws DecoderException {
        long i = part.sequenceId();
        if (!parts.containsKey(i)) {
            parts.put(i, new PartialMergeData(i, part));
        }
        PartialMergeData partialMergeData = parts.get(i);
        if (parts.containsKey(i)) {
            partialMergeData.load(part);
        }

        if (partialMergeData.isComplete()) {
            try {
                partialMergeData.integrityCheck();
                PacketBuffer packetBuffer = new PacketBuffer(partialMergeData.parts());
                list.add(new PacketBufferInfo(partialMergeData.packetId(), partialMergeData.sequenceId(), packetBuffer));
            } catch (PacketIntegrityException e) {
                throw new DecoderException(e);
            }

        }
    }
}
