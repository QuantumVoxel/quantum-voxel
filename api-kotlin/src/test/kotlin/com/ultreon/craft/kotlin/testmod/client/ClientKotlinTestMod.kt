package com.ultreon.quantum.kotlin.testmod.client

import com.ultreon.quantum.client.ClientModInit
import com.ultreon.quantum.client.api.events.gui.ScreenEvents
import com.ultreon.quantum.client.gui.screens.TitleScreen
import com.ultreon.quantum.client.gui.widget.Button
import com.ultreon.quantum.client.gui.widget.TextButton
import com.ultreon.quantum.events.api.ValueEventResult
import com.ultreon.quantum.kotlin.api.button
import com.ultreon.quantum.kotlin.api.literal
import com.ultreon.quantum.util.Color
import com.ultreon.quantum.util.Color.hex
import com.ultreon.quantum.util.Color.rgb

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