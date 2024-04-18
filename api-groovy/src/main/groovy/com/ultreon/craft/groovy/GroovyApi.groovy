package com.ultreon.quantum.groovy

import com.ultreon.quantum.client.QuantumClient
import com.ultreon.quantum.server.QuantumServer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

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
