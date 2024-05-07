//package dev.ultreon.quantum.network.packets.c2s;
//
//import dev.ultreon.quantum.network.PacketBuffer;
//import dev.ultreon.quantum.network.PacketContext;
//import dev.ultreon.quantum.network.packets.Packet;
//import dev.ultreon.quantum.network.server.InGameServerPacketHandler;
//import dev.ultreon.quantum.world.container.ContainerInteraction;
//
//public class C2SContainerClickPacket extends Packet<InGameServerPacketHandler> {
//    private final int slot;
//    private final ContainerInteraction interaction;
//
//    public C2SContainerClickPacket(int slot, ContainerInteraction interaction) {
//        this.slot = slot;
//        this.interaction = interaction;
//    }
//
//    public C2SContainerClickPacket(PacketBuffer buffer) {
//        this.slot = buffer.readVarInt();
//        this.interaction = ContainerInteraction.values()[buffer.readByte()];
//    }
//
//    @Override
//    public void toBytes(PacketBuffer buffer) {
//        buffer.writeVarInt(slot);
//        buffer.writeByte(interaction.ordinal());
//    }
//
//    @Override
//    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
//        handler.handleContainerClick(slot, interaction);
//    }
//
//}
