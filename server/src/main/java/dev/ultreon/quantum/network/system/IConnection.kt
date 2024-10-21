package dev.ultreon.quantum.network.system

import com.google.errorprone.annotations.CanIgnoreReturnValue
import dev.ultreon.quantum.log.Logger
import dev.ultreon.quantum.log.LoggerFactory
import dev.ultreon.quantum.network.PacketHandler
import dev.ultreon.quantum.network.PacketListener
import dev.ultreon.quantum.network.packets.Packet
import dev.ultreon.quantum.network.stage.PacketStage
import dev.ultreon.quantum.network.stage.PacketStages
import dev.ultreon.quantum.server.player.ServerPlayer
import dev.ultreon.quantum.text.TextObject
import dev.ultreon.quantum.util.Result
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger

interface IConnection<OurHandler : PacketHandler, TheirHandler : PacketHandler> :
  Closeable {
  @CanIgnoreReturnValue
  fun send(packet: Packet<out TheirHandler>) {
    send(packet, null)
  }

  @Deprecated("")
  @CanIgnoreReturnValue
  fun send(packet: Packet<out TheirHandler>, resultListener: PacketListener?)

  val isCompressed: Boolean

  fun disconnect(message: String)

  fun on3rdPartyDisconnect(message: String): Result<Void>

  fun queue(handler: Runnable)

  fun start()

  fun moveTo(stage: PacketStage, handler: OurHandler)

  val isConnecting: Boolean
    get() = false

  val isConnected: Boolean

  fun tick() {
  }

  val isMemoryConnection: Boolean

  fun initiate(handler: OurHandler, packetToThem: Packet<out TheirHandler>?) {
    this.moveTo(PacketStages.LOGIN, handler)
    if (packetToThem != null) this.send(packetToThem)
  }

  fun setReadOnly()

  fun setPlayer(player: ServerPlayer?)

  fun disconnect(message: TextObject) {
    disconnect(message.text)
  }

  val ping: Long

  fun onPing(ping: Long)

  companion object {
    @JvmStatic
    val logger: Logger = LoggerFactory.getLogger("NetConnections")
    @JvmStatic
    val rx: AtomicInteger = AtomicInteger()
    @JvmStatic
    val tx: AtomicInteger = AtomicInteger()
  }
}
