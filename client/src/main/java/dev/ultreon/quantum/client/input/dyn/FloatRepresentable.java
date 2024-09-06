package dev.ultreon.quantum.client.input.dyn;

import dev.ultreon.quantum.client.input.controller.ControllerSignedFloat;
import dev.ultreon.quantum.client.input.controller.ControllerUnsignedFloat;

public interface FloatRepresentable {
    ControllerSignedFloat asSignedFloat();
    ControllerUnsignedFloat asUnsignedFloat();
}
