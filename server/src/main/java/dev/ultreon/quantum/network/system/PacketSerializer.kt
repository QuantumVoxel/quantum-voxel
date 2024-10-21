package dev.ultreon.quantum.network.system

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import dev.ultreon.quantum.network.PacketIO
import dev.ultreon.quantum.network.packets.Packet
import java.lang.reflect.InvocationTargetException

class PacketSerializer(val kryo: Kryo) :
  Serializer<Packet<*>>() {
  override fun write(kryo: Kryo, output: Output, o: Packet<*>) {
    val packetIO = PacketIO(null, output)
    o.toBytes(packetIO)
  }

  override fun read(kryo: Kryo, input: Input, aClass: Class<Packet<*>>): Packet<*> {
    require(Packet::class.java.isAssignableFrom(aClass)) { "Class " + aClass.name + " is not a valid packet class" }
    try {
      val constructor = aClass.getConstructor(
        PacketIO::class.java
      )
      constructor.isAccessible = true
      return constructor.newInstance(PacketIO(input, null))
    } catch (e: NoSuchMethodException) {
      throw RuntimeException(e)
    } catch (e: IllegalAccessException) {
      throw RuntimeException(e)
    } catch (e: InstantiationException) {
      throw RuntimeException(e)
    } catch (e: InvocationTargetException) {
      throw RuntimeException(e)
    }
  }
}
