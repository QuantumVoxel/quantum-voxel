package com.ultreon.quantum.kotlin.dsl

import com.ultreon.quantum.client.gui.icon.Icon
import com.ultreon.quantum.client.gui.widget.IconButton

class IconButtonDSL(val icon: Icon) {
    private var position: () -> PositionDSL = { PositionDSL() }

    infix fun position(dsl: PositionDSL.() -> Unit) {
        this.position = { PositionDSL().apply(dsl) }
    }

    internal fun build() = IconButton.of(icon).position(position)
}
