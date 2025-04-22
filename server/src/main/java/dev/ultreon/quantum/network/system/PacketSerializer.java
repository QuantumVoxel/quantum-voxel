//package dev.ultreon.quantum.network.system;
//
//import dev.ultreon.quantum.network.PacketIO;
//import dev.ultreon.quantum.network.packets.Packet;
//
//import java.lang.reflect.InvocationTargetException;
//
//public class PacketSerializer extends Serializer<Packet<?>> {
//    private final Kryo kryo;
//
//    public PacketSerializer(Kryo kryo) {
//        this.kryo = kryo;
//    }
//
//    @Override
//    public void write(Kryo kryo, Output output, Packet<?> o) {
//        PacketIO packetIO = new PacketIO(null, output);
//        o.toBytes(packetIO);
//    }
//
//    @Override
//    public Packet<?> read(Kryo kryo, Input input, Class<Packet<?>> aClass) {
//        if (!Packet.class.isAssignableFrom(aClass)) throw new IllegalArgumentException("Class " + aClass.getName() + " is not a valid packet class");
//        try {
//            var constructor = aClass.getMethod("read", PacketIO.class);
//            constructor.setAccessible(true);
//            return (Packet<?>) constructor.invoke(null, new PacketIO(input, null));
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public Kryo getKryo() {
//        return kryo;
//    }
//}
