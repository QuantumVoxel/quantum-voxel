package dev.ultreon.quantum.network.system

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import dev.ultreon.quantum.network.Networker
import dev.ultreon.quantum.network.client.ClientPacketHandler
import dev.ultreon.quantum.network.server.LoginServerPacketHandler
import dev.ultreon.quantum.network.server.ServerPacketHandler
import dev.ultreon.quantum.network.stage.PacketStages
import dev.ultreon.quantum.server.QuantumServer
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress

class TcpNetworker(private val server: QuantumServer, host: InetAddress?, port: Int) : Listener(),
  Networker {
  private val connections: MutableMap<Connection, ServerTcpConnection> = HashMap()

  private val kryoServer = Server(2 * 1024 * 1024, 2 * 1024 * 1024)

  init {
    kryoServer.addListener(this)
    kryoServer.kryo.setReferences(false)
    kryoServer.kryo.isRegistrationRequired = false
    kryoServer.kryo.setDefaultSerializer(PacketIOSerializerFactory())

    kryoServer.bind(InetSocketAddress(host, port), null)

    kryoServer.start()
  }

  override fun connected(connection: Connection) {
    super.connected(connection)

    if (connections.containsKey(connection)) return

    connection.setName("QuantumConn:" + connection.remoteAddressTCP.address)

    val conn = ServerTcpConnection(connection, this.kryoServer, this.server)
    conn.moveTo(
      PacketStages.LOGIN, LoginServerPacketHandler(
        this.server, conn
      )
    )
    connections[connection] = conn
  }

  override fun received(connection: Connection, `object`: Any) {
    super.received(connection, `object`)
  }

  override fun disconnected(connection: Connection) {
    super.disconnected(connection)

    connections.remove(connection)
  }

  @Throws(IOException::class)
  override fun close() {
    for (connection in connections.values) {
      connection.disconnect("Server shutting down")
    }

    kryoServer.close()
  }

  override fun isRunning(): Boolean {
    return kryoServer.updateThread.isAlive
  }

  override fun getConnections(): List<IConnection<ServerPacketHandler, ClientPacketHandler>> {
    return connections.values.stream().map { c: ServerTcpConnection -> c }.toList()
  }

  override fun tick() {
    for (connection in connections.values) {
      connection.tick()
    }
  }
}
