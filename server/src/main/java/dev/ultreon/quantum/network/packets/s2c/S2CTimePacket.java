package dev.ultreon.quantum.network.packets.s2c;

import dev.ultreon.quantum.network.PacketContext;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.client.InGameClientPacketHandler;
import dev.ultreon.quantum.network.packets.Packet;

public class S2CTimePacket extends Packet<InGameClientPacketHandler> {
    private final Operation operation;
    private final int time;

    public S2CTimePacket(Operation operation, int time) {
        this.operation = operation;
        this.time = time;
    }

    public S2CTimePacket(PacketIO buffer) {
        this.operation = Operation.values()[buffer.readVarInt()];
        this.time = buffer.readInt();
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(operation.ordinal());
        buffer.writeInt(time);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onTimeChange(ctx, operation, time);
    }

    public enum Operation {
        SET,
        ADD,
        SUB
    }

    public int getTime() {
        return time;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return "S2CTimePacket{" +
                "operation=" + operation +
                ", time=" + time +
                '}';
    }
}
