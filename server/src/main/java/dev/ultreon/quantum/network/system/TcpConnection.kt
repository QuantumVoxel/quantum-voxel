@file:Suppress("UNCHECKED_CAST")

package dev.ultreon.quantum.network.system

import com.badlogic.gdx.utils.Pool
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.KryoNetException
import com.esotericsoftware.kryonet.Listener
import com.google.common.collect.Queues
import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.sun.jdi.connect.spi.ClosedConnectionException
import dev.ultreon.quantum.network.PacketContext
import dev.ultreon.quantum.network.PacketData
import dev.ultreon.quantum.network.PacketHandler
import dev.ultreon.quantum.network.PacketListener
import dev.ultreon.quantum.network.packets.Packet
import dev.ultreon.quantum.network.stage.PacketStage
import dev.ultreon.quantum.server.QuantumServer
import dev.ultreon.quantum.server.player.ServerPlayer
import dev.ultreon.quantum.util.Env
import dev.ultreon.quantum.util.Result
import java.io.Closeable
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.util.*
import java.util.concurrent.Executor

abstract class TcpConnection<OurHandler : PacketHandler, TheirHandler : PacketHandler>(
  protected val connection: Connection, private val executor: Executor
) :
  Listener(), IConnection<OurHandler, TheirHandler>,
  Closeable {
  private val packetQueue: Queue<Packet<out TheirHandler>> = Queues.synchronizedQueue(ArrayDeque())
  private val senderThread: Thread
  override var isCompressed: Boolean = false
  private var ourPacketData: PacketData<OurHandler>? = null
  private var theirPacketData: PacketData<TheirHandler>? = null
  private var handler: OurHandler? = null
  var isReadOnly: Boolean = false
    private set
  override var ping: Long = 0
    protected set

  init {
    connection.addListener(this)

    senderThread = Thread {
      try {
        this.sender()
      } catch (e: ClosedChannelException) {
        // Ignored
      } catch (_: ClosedConnectionException) {
      } catch (e: IOException) {
        this.disconnect("Connection interrupted!")
      }
    }

    senderThread.isDaemon = true
  }

  @Throws(IOException::class)
  private fun sender() {
    while (connection.isConnected && isRunning) {
      val packet: Packet<*>? = packetQueue.poll()
      if (isReadOnly) return
      if (packet != null) {
        connection.sendTCP(packet)
      }
    }
  }

  protected abstract val isRunning: Boolean

  protected abstract fun getPlayer(): ServerPlayer

  private fun iLuvGenerics(decode: Packet<OurHandler>, player: ServerPlayer) {
    ourPacketData!!.handle(
      decode, PacketContext(player, this, Env.SERVER),
      this.handler
    )
  }

  @CanIgnoreReturnValue
  override fun send(packet: Packet<out TheirHandler?>) {
    if (this.isReadOnly) throw ReadOnlyConnectionException()
    require(theirPacketData!!.getId(packet) >= 0) { "Invalid packet: " + packet.javaClass.name }

    packetQueue.add(packet)
  }

  @Deprecated("")
  override fun send(packet: Packet<out TheirHandler>, resultListener: PacketListener?) {
    if (this.isReadOnly) throw ReadOnlyConnectionException()
    require(theirPacketData!!.getId(packet) >= 0) { "Invalid packet: " + packet.javaClass.name }


    packetQueue.add(packet)
  }

  override fun disconnect(message: String) {
    this.send(this.getDisconnectPacket(message))

    try {
      connection.close()
    } catch (e: KryoNetException) {
      val voidResult = this.on3rdPartyDisconnect(e.message ?: "null")
      if (voidResult.isFailure) e.addSuppressed(voidResult.failure)

      QuantumServer.LOGGER.error("Failed to close socket", e)
    }
  }

  override fun on3rdPartyDisconnect(message: String): Result<Void> {
    return Result.ok()
  }

  override fun received(connection: Connection, `object`: Any) {
    super.received(connection, `object`)

    try {
      when (`object`) {
        is Packet<*> -> {
          iLuvGenerics(`object` as Packet<OurHandler>, getPlayer())
        }

        is FrameworkMessage.KeepAlive -> {
          connection.sendTCP(FrameworkMessage.KeepAlive())
        }

        else -> {
          connection.close()
        }
      }
    } catch (e: Exception) {
      IConnection.logger.error("Failed to handle packet", e)
    }
  }

  override fun disconnected(connection: Connection) {
    this.on3rdPartyDisconnect("Connection closed!")
  }

  protected abstract fun getDisconnectPacket(message: String): Packet<TheirHandler>

  override fun queue(handler: Runnable) {
    executor.execute(handler)
  }

  override fun start() {
    senderThread.start()
  }

  override fun moveTo(stage: PacketStage, handler: OurHandler) {
    this.ourPacketData = this.getOurData(stage)
    this.theirPacketData = this.getTheirData(stage)

    this.handler = handler
  }

  override val isConnected: Boolean
    get() = connection.isConnected

  override val isMemoryConnection: Boolean
    get() = false

  protected abstract fun getOurData(stage: PacketStage): PacketData<OurHandler>

  protected abstract fun getTheirData(stage: PacketStage): PacketData<TheirHandler>

  @Throws(IOException::class)
  override fun close() {
    connection.close()
  }

  override fun setReadOnly() {
    this.isReadOnly = true
  }

  override fun setPlayer(player: ServerPlayer?) {
  }

  override fun onPing(ping: Long) {
  }

  companion object {
    val sequencePool: Pool<Long> = object : Pool<Long>() {
      private var next: Long = 0

      override fun newObject(): Long {
        return next++
      }
    }

    @JvmStatic
    fun handleReply(sequenceId: Long) {
      sequencePool.free(sequenceId)
    }
  }
}
