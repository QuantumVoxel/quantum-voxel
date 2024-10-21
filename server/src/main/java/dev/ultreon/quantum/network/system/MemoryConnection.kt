@file:Suppress("UNCHECKED_CAST")

package dev.ultreon.quantum.network.system

import dev.ultreon.quantum.CommonConstants
import dev.ultreon.quantum.crash.ApplicationCrash
import dev.ultreon.quantum.crash.CrashLog
import dev.ultreon.quantum.network.*
import dev.ultreon.quantum.network.packets.Packet
import dev.ultreon.quantum.network.stage.PacketStage
import dev.ultreon.quantum.server.player.ServerPlayer
import dev.ultreon.quantum.util.Result
import dev.ultreon.quantum.util.SanityCheckException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor

abstract class MemoryConnection<OurHandler : PacketHandler, TheirHandler : PacketHandler>(
  private var otherSide: MemoryConnection<TheirHandler, OurHandler>?,
  private val executor: Executor,
  private val thread: Thread
) :
  IConnection<OurHandler, TheirHandler> {
  private var handler: OurHandler? = null

  private var ourPacketData: PacketData<OurHandler>? = null
  var theirPacketData: PacketData<TheirHandler>? = null
    private set
  private var readOnly = false

  private val sendQueue: Queue<Packet<out TheirHandler>> = ConcurrentLinkedQueue()
  private val receiveQueue: Queue<Packet<out OurHandler>> = ConcurrentLinkedQueue()

  init {
    val receiver = Thread {
      try {
        while (true) {
          val packet = receiveQueue.poll() ?: continue
          this.received(packet, null)

          Thread.sleep(5)
        }
      } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
      }
    }

    val sender = Thread {
      while (true) {
        val packet = sendQueue.poll() ?: continue

        try {
        } catch (e: IOException) {
          throw RuntimeException(e)
        }
        IConnection.tx.decrementAndGet()
      }
    }

    receiver.isDaemon = true
    sender.isDaemon = true

    receiver.start()
    sender.start()
  }

  private fun receive(id: Int, ourPacket: ByteArray) {
    IConnection.rx.incrementAndGet()
    val bis = ByteArrayInputStream(ourPacket)
    val io = PacketIO(bis, null)
    val packet = ourPacketData!!.decode(id, io)
    this.received(packet as Packet<out OurHandler>, null)
  }

  override fun onPing(ping: Long) {
    // No-op
  }

  override fun send(packet: Packet<out TheirHandler>) {
    if (!handler!!.isAsync && isRunningAsync) if (otherSide == null || this.readOnly) throw ReadOnlyConnectionException()
    val id = theirPacketData!!.getId(packet)
    require(id >= 0) { "Invalid packet: " + packet.javaClass.name }

    IConnection.tx.incrementAndGet()
    val bos = ByteArrayOutputStream()
    val io = PacketIO(null, bos)
    packet.toBytes(io)
    bos.close()

    theirPacketData!!.encode(packet, io)

    otherSide!!.receive(id, bos.toByteArray())

    if (sendQueue.size >= 5000) {
      val crashLog = CrashLog("Too many packets in send queue", Throwable(":("))
      crashLog.add("Send queue size", sendQueue.size)
      throw ApplicationCrash(crashLog)
    }
  }

  private val isRunningAsync: Boolean
    get() = thread.threadId() == Thread.currentThread().threadId()

  @Deprecated("", ReplaceWith("this.send(packet)"))
  override fun send(packet: Packet<out TheirHandler>, resultListener: PacketListener?) {
    this.send(packet)
  }

  override fun queue(handler: Runnable) {
    executor.execute(handler)
  }

  protected open fun received(packet: Packet<out OurHandler>, resultListener: PacketListener?) {
    try {
      if (handler == null) throw SanityCheckException("No handler set")
      require(ourPacketData!!.getId(packet) >= 0) { "Invalid packet: " + packet.javaClass.name }
      (packet as Packet<OurHandler>).handle(createPacketContext(), handler)
      IConnection.rx.decrementAndGet()
    } catch (e: Throwable) {
      resultListener?.onFailure()
      CommonConstants.LOGGER.error("Failed to handle packet", e)
      IConnection.rx.decrementAndGet()
      return
    }

    resultListener?.onSuccess()
  }

  override fun disconnect(message: String) {
    otherSide!!.on3rdPartyDisconnect(message)
  }

  abstract override fun on3rdPartyDisconnect(message: String): Result<Void>

  protected abstract fun createPacketContext(): PacketContext

  protected fun getPlayer(): ServerPlayer? {
    return null
  }

  fun setHandler(handler: OurHandler) {
    this.handler = handler
  }

  override val isCompressed: Boolean
    get() {
      return false
    }

  override fun start() {
    checkNotNull(otherSide) { "Cannot start connection without the other side" }

    // TODO: Implement
  }

  override fun moveTo(stage: PacketStage, handler: OurHandler) {
    this.ourPacketData = this.getOurData(stage)
    this.theirPacketData = this.getTheirData(stage)

    this.handler = handler
  }

  override val isConnected: Boolean
    get() {
      return otherSide != null && otherSide!!.isConnected
    }

  override val isMemoryConnection: Boolean
    get() {
      return true
    }

  protected abstract fun getOurData(stage: PacketStage): PacketData<OurHandler>

  protected abstract fun getTheirData(stage: PacketStage): PacketData<TheirHandler>

  fun setOtherSide(otherSide: MemoryConnection<TheirHandler, OurHandler>) {
    this.otherSide = otherSide
  }

  override fun close() {
  }

  override fun setReadOnly() {
    this.readOnly = true
  }

  override fun setPlayer(player: ServerPlayer?) {
  }

  override val ping: Long
    get() {
      return 0
    }

  companion object {
    val rx: Int
      get() = IConnection.rx.get()
  }
}
