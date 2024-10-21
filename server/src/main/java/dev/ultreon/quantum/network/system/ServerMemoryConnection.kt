package dev.ultreon.quantum.network.system

import dev.ultreon.quantum.CommonConstants
import dev.ultreon.quantum.network.PacketContext
import dev.ultreon.quantum.network.PacketData
import dev.ultreon.quantum.network.PacketListener
import dev.ultreon.quantum.network.client.ClientPacketHandler
import dev.ultreon.quantum.network.packets.Packet
import dev.ultreon.quantum.network.server.ServerPacketHandler
import dev.ultreon.quantum.network.stage.PacketStage
import dev.ultreon.quantum.server.QuantumServer
import dev.ultreon.quantum.server.player.ServerPlayer
import dev.ultreon.quantum.util.Env
import dev.ultreon.quantum.util.Result

class ServerMemoryConnection(
  otherSide: MemoryConnection<ClientPacketHandler, ServerPacketHandler>,
  server: QuantumServer,
  thread: Thread
) :
  MemoryConnection<ServerPacketHandler, ClientPacketHandler>(otherSide, server, thread) {
  private var player: ServerPlayer? = null

  override fun setPlayer(player: ServerPlayer?) {
    this.player = player
  }

  override fun received(packet: Packet<out ServerPacketHandler>, resultListener: PacketListener?) {
    QuantumServer.invoke {
      try {
        super.received(packet, resultListener)
      } catch (e: Exception) {
        CommonConstants.LOGGER.warn("Packet failed to receive!", e)
      }
    }
  }

  override fun on3rdPartyDisconnect(message: String): Result<Void> {
    if (player != null) player!!.onDisconnect(message)
    return Result.ok()
  }

  override fun createPacketContext(): PacketContext {
    return PacketContext(player, this, Env.CLIENT)
  }

  override fun getOurData(stage: PacketStage): PacketData<ServerPacketHandler> {
    return stage.serverPackets
  }

  override fun getTheirData(stage: PacketStage): PacketData<ClientPacketHandler> {
    return stage.clientPackets
  }
}
