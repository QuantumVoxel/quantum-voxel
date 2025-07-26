package dev.ultreon.quantum.network.system;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import dev.ultreon.quantum.network.PacketIO;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.RegistryHandle;
import dev.ultreon.quantum.server.QuantumServer;

import java.lang.reflect.InvocationTargetException;

public class PacketSerializer extends Serializer<Packet<?>> {
    private final Kryo kryo;
    private RegistryHandle registryHandle;

    public PacketSerializer(Kryo kryo, RegistryHandle registryHandle) {
        this.kryo = kryo;
        this.registryHandle = registryHandle;
    }

    @Override
    public void write(Kryo kryo, Output output, Packet<?> o) {
        PacketIO packetIO = new PacketIO(null, output.getOutputStream(), registryHandle);
        o.toBytes(packetIO);
    }

    @Override
    public Packet<?> read(Kryo kryo, Input input, Class<Packet<?>> aClass) {
        if (!Packet.class.isAssignableFrom(aClass)) throw new IllegalArgumentException("Class " + aClass.getName() + " is not a valid packet class");
        try {
            var constructor = aClass.getMethod("read", PacketIO.class);
            constructor.setAccessible(true);
            return (Packet<?>) constructor.invoke(null, new PacketIO(input.getInputStream(), null, registryHandle));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Kryo getKryo() {
        return kryo;
    }
}
