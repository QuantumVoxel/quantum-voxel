package dev.ultreon.quantum.groovy

import dev.ultreon.quantum.client.QuantumClient
import dev.ultreon.quantum.server.QuantumServer
import net.fabricmc.loader.api.FabricLoader
import dev.ultreon.quantum.log.LoggerFactory

class GroovyApi {
    def fabricLoader = FabricLoader.instance
    def logger

    def client = { -> QuantumClient.get() }
    def server = { -> QuantumServer.get() }

    private def scriptName

    GroovyApi(scriptName) {
        this.scriptName = scriptName
        logger = LoggerFactory.getLogger("Groovy:${scriptName}")
    }

    def getScriptName() { scriptName }
}
