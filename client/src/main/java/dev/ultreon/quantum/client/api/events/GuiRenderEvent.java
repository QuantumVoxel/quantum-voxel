package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.client.gui.Renderer;

public interface GuiRenderEvent extends ClientEvent {
    Renderer getRenderer();
    float getDeltaTime();
}
