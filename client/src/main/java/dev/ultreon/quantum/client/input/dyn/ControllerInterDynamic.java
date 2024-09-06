package dev.ultreon.quantum.client.input.dyn;

import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
import dev.ultreon.quantum.client.input.controller.ControllerBoolean;
import dev.ultreon.quantum.client.input.controller.ControllerSignedFloat;
import dev.ultreon.quantum.client.input.controller.ControllerUnsignedFloat;
import dev.ultreon.quantum.client.input.controller.ControllerVec2;

public interface ControllerInterDynamic<T> extends
        ControllerDynamic, BooleanConvertible<T>, SignedFloatConvertible<T>, UnsignedFloatConvertible<T>, Vec2Convertible<T> {

    @SuppressWarnings("unchecked")
    default <V extends ControllerInterDynamic<?>> V as(V mapping) {
        if (mapping instanceof ControllerBoolean) {
            return (V) this.asBoolean();
        } else if (mapping instanceof ControllerSignedFloat) {
            return (V) this.asSignedFloat();
        } else if (mapping instanceof ControllerUnsignedFloat) {
            return (V) this.asUnsignedFloat();
        } else if (mapping instanceof ControllerVec2) {
            return (V) this.asVec2();
        } else {
            throw new IllegalArgumentException("Cannot convert " + this + " to " + mapping);
        }
    }

    ControllerIcon getIcon();
}
