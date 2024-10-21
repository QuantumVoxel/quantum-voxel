package dev.ultreon.quantum.network.system

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.factories.SerializerFactory
import dev.ultreon.quantum.network.packets.Packet

class PacketIOSerializerFactory : SerializerFactory {
  override fun makeSerializer(kryo: Kryo, aClass: Class<*>): Serializer<Packet<*>> {
    if (Packet::class.java.isAssignableFrom(aClass)) {
      return PacketSerializer(kryo)
    }

    throw IllegalArgumentException("Class " + aClass.name + " is not a valid packet class")
  }
}
