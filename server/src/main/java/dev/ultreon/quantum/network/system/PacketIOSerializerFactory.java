package dev.ultreon.quantum.network.system;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.factories.SerializerFactory;
import dev.ultreon.quantum.network.packets.Packet;
import dev.ultreon.quantum.registry.RegistryHandle;

public class PacketIOSerializerFactory implements SerializerFactory {
    private final RegistryHandle registryHandle;

    public PacketIOSerializerFactory(RegistryHandle registryHandle) {
        this.registryHandle = registryHandle;
    }

    @Override
    public Serializer<Packet<?>> makeSerializer(Kryo kryo, Class<?> aClass) {
        if (Packet.class.isAssignableFrom(aClass)) {
            return new PacketSerializer(kryo, registryHandle);
        }

        throw new IllegalArgumentException("Class " + aClass.getName() + " is not a valid packet class");
    }
}
