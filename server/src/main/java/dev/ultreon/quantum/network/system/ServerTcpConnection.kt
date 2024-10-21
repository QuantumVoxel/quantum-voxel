package dev.ultreon.quantum.network.system

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Server
import dev.ultreon.quantum.network.PacketData
import dev.ultreon.quantum.network.client.ClientPacketHandler
import dev.ultreon.quantum.network.packets.Packet
import dev.ultreon.quantum.network.packets.s2c.S2CDisconnectPacket
import dev.ultreon.quantum.network.server.ServerPacketHandler
import dev.ultreon.quantum.network.stage.PacketStage
import dev.ultreon.quantum.server.QuantumServer
import dev.ultreon.quantum.server.player.ServerPlayer
import dev.ultreon.quantum.util.Result
import java.io.IOException

class ServerTcpConnection(connection: Connection, private val kryoServer: Server, val server: QuantumServer) :
  TcpConnection<ServerPacketHandler, ClientPacketHandler>(
    connection,
    server
  ) {
  private var player: ServerPlayer? = null

  init {
    this.start()
  }

  override fun getDisconnectPacket(message: String): Packet<ClientPacketHandler> {
    return S2CDisconnectPacket(message)
  }

  override val isRunning: Boolean
    get() {
      return server.isRunning
    }

  override fun on3rdPartyDisconnect(message: String): Result<Void> {
    try {
      this.close()
    } catch (e: IOException) {
      return Result.failure(e)
    }
    return Result.ok(null)
  }

  override fun getOurData(stage: PacketStage): PacketData<ServerPacketHandler> {
    return stage.serverPackets
  }

  override fun getTheirData(stage: PacketStage): PacketData<ClientPacketHandler> {
    return stage.clientPackets
  }

  public override fun getPlayer(): ServerPlayer {
    var player = this.player
    if (player == null) player = server.playerManager.byConnection(this.connection)

    return player!!
  }

  override fun setPlayer(player: ServerPlayer?) {
    this.player = player
  }

  override fun onPing(ping: Long) {
    this.ping = ping
  }
}
