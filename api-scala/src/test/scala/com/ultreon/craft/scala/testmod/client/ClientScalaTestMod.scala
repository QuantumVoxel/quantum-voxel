package dev.ultreon.quantum.scala.testmod.client

import dev.ultreon.quantum.client.api.events.ClientLifecycleEvents
import dev.ultreon.quantum.client.{ClientModInit, QuantumClient}
import net.fabricmc.api.ModInitializer

class ClientScalaTestMod extends ClientModInit {
  override def onInitializeClient(): Unit = {
    ClientLifecycleEvents.CLIENT_STARTED.subscribe((_: QuantumClient) => {
      println("Game loaded!")
    })
  }
}
