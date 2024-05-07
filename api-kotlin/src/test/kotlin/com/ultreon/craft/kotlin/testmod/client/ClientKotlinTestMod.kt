package dev.ultreon.quantum.kotlin.testmod.client

import dev.ultreon.quantum.client.ClientModInit
import dev.ultreon.quantum.client.api.events.gui.ScreenEvents
import dev.ultreon.quantum.client.gui.screens.TitleScreen
import dev.ultreon.quantum.client.gui.widget.Button
import dev.ultreon.quantum.client.gui.widget.TextButton
import dev.ultreon.quantum.events.api.ValueEventResult
import dev.ultreon.quantum.kotlin.api.button
import dev.ultreon.quantum.kotlin.api.literal
import dev.ultreon.quantum.util.Color
import dev.ultreon.quantum.util.Color.hex
import dev.ultreon.quantum.util.Color.rgb

class ClientKotlinTestMod : ClientModInit {
    override fun onInitializeClient() {
        println("Hello from Kotlin!")

        ScreenEvents.OPEN.subscribe {
            if (it is TitleScreen) {
                it.add(button {
                    this.text = "Hello".literal
                    this.textColor = rgb(1.0f, 1.0f, 1.0f)

                    this.clicked {
                        println("Hello from Kotlin!")
                    }
                })
            }

            return@subscribe ValueEventResult.pass()
        }
    }

}