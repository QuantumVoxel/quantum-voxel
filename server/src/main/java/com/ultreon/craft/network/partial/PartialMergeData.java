package com.ultreon.craft.network.partial;

import com.google.common.collect.Lists;
import com.ultreon.craft.network.PacketIntegrityException;

import java.util.List;
import java.util.Objects;

public final class PartialMergeData {
    private final long sequenceId;
    private final List<PartialPacket> parts;
    private final int totalLength;
    private int length = 0;

    public PartialMergeData(long sequenceId, PartialPacket part) {
        this.sequenceId = sequenceId;
        this.parts = Lists.newArrayList(part);
        this.totalLength = part.dataLength();
    }

    public void load(PartialPacket part) {
        parts.add(part);
        length += part.dataLength();
    }

    public int length() {
        return length;
    }

    public int totalLength() {
        return totalLength;
    }

    public long sequenceId() {
        return sequenceId;
    }

    public List<PartialPacket> parts() {
        return parts;
    }

    public boolean isComplete() {
        return length >= totalLength;
    }

    public void integrityCheck() throws PacketIntegrityException {
        if (length == totalLength) return;

        throw new PacketIntegrityException("Partial packet integrity check failed");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PartialMergeData) obj;
        return this.sequenceId == that.sequenceId &&
                Objects.equals(this.parts, that.parts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceId, parts);
    }

    @Override
    public String toString() {
        return "PartialMergeData[" +
                "sequenceId=" + sequenceId + ", " +
                "parts=" + parts + ']';
    }

    public int packetId() {
        return parts.get(0).packetId();
    }
}
