package com.ultreon.quantum.groovy.testmod.client

import com.ultreon.quantum.client.ClientModInit
import com.ultreon.quantum.client.api.events.ClientLifecycleEvents

class ClientGroovyTestMod implements ClientModInit {
    @Override
    void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.subscribe {
            def name = it.user.name()

            println "Hello ${name}!"
        }
    }
}
