package dev.ultreon.quantum.client.input.controller;

import com.badlogic.gdx.math.Vector2;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis;
import org.intellij.lang.annotations.MagicConstant;

public enum ControllerSignedFloat implements ControllerInterDynamic<Float> {
    TriggerMagnitude,
    LeftStickMagnitude,
    RightStickMagnitude,
    DpadMagnitude,
    LeftTrigger,
    RightTrigger,
    Triggers,
    Shoulders,
    LeftStickX,
    LeftStickY,
    RightStickX,
    RightStickY,
    DpadX,
    DpadY,
    Unknown;

    public float getValue() {
        return switch (this) {
            case LeftTrigger -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftTrigger) * 2 - 1;
            case RightTrigger -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightTrigger) * 2 - 1;
            case Triggers -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftTrigger) - QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightTrigger);
            case Shoulders -> (QuantumClient.get().controllerInput.isButtonPressed(ControllerBoolean.LeftShoulder) ? -1 : 0) + (QuantumClient.get().controllerInput.isButtonPressed(ControllerBoolean.RightShoulder) ? 1 : 0);
            case LeftStickX -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickX);
            case LeftStickY -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.LeftStickY);
            case RightStickX -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickX);
            case RightStickY -> QuantumClient.get().controllerInput.getAxis(ControllerSignedFloat.RightStickY);
            case DpadX -> of(ControllerBoolean.DpadLeft, ControllerBoolean.DpadRight);
            case DpadY -> of(ControllerBoolean.DpadDown, ControllerBoolean.DpadUp);
            case LeftStickMagnitude -> ControllerVec2.LeftStick.getMagnitude() * 2 - 1;
            case RightStickMagnitude -> ControllerVec2.RightStick.getMagnitude() * 2 - 1;
            case DpadMagnitude -> ControllerVec2.Dpad.getMagnitude() * 2 - 1;
            case TriggerMagnitude -> ControllerVec2.Triggers.getMagnitude() * 2 - 1;
            default -> 0;
        };
    }

    private float of(ControllerBoolean a, ControllerBoolean b) {
        float v = 0;
        if (a.getValue()) v--;
        if (b.getValue()) v++;
        return v;
    }

    @Override
    public ControllerBoolean asBoolean() {
        return switch (this) {
            case LeftTrigger -> ControllerBoolean.LeftTrigger;
            case RightTrigger -> ControllerBoolean.RightTrigger;
            case Triggers, TriggerMagnitude -> ControllerBoolean.AnyTrigger;
            case Shoulders -> ControllerBoolean.AnyShoulder;
            case LeftStickX -> ControllerBoolean.LeftStickLeft;
            case LeftStickY -> ControllerBoolean.LeftStickUp;
            case RightStickX -> ControllerBoolean.RightStickLeft;
            case RightStickY -> ControllerBoolean.RightStickUp;
            case DpadX -> ControllerBoolean.DpadX;
            case DpadY -> ControllerBoolean.DpadY;
            case LeftStickMagnitude -> ControllerBoolean.LeftStickUsed;
            case RightStickMagnitude -> ControllerBoolean.RightStickUsed;
            case DpadMagnitude -> ControllerBoolean.DpadUsed;
            default -> ControllerBoolean.Unknown;
        };
    }

    @Override
    public Pair<ControllerBoolean, Boolean> asBoolean(Float value) {
        return switch (this) {
            case LeftTrigger -> new Pair<>(ControllerBoolean.LeftTrigger, value > 0);
            case RightTrigger -> new Pair<>(ControllerBoolean.RightTrigger, value > 0);
            case LeftStickX -> new Pair<>(ControllerBoolean.LeftStickLeft, value < 0);
            case LeftStickY -> new Pair<>(ControllerBoolean.LeftStickUp, value > 0);
            case RightStickX -> new Pair<>(ControllerBoolean.RightStickLeft, value < 0);
            case RightStickY -> new Pair<>(ControllerBoolean.RightStickUp, value > 0);
            case DpadX -> new Pair<>(ControllerBoolean.DpadX, value > 0);
            case DpadY -> new Pair<>(ControllerBoolean.DpadY, value > 0);
            case LeftStickMagnitude -> new Pair<>(ControllerBoolean.LeftStickUsed, value > 0);
            case RightStickMagnitude -> new Pair<>(ControllerBoolean.RightStickUsed, value > 0);
            case DpadMagnitude -> new Pair<>(ControllerBoolean.DpadUsed, value > 0);
            case TriggerMagnitude -> new Pair<>(ControllerBoolean.AnyTrigger, value > 0);
            default -> new Pair<>(ControllerBoolean.Unknown, false);
        };
    }

    @Override
    public ControllerUnsignedFloat asUnsignedFloat() {
        return switch (this) {
            case LeftStickX -> ControllerUnsignedFloat.LeftStickX;
            case LeftStickY -> ControllerUnsignedFloat.LeftStickY;
            case RightStickX -> ControllerUnsignedFloat.RightStickX;
            case RightStickY -> ControllerUnsignedFloat.RightStickY;
            case LeftStickMagnitude -> ControllerUnsignedFloat.LeftStickMagnitude;
            case RightStickMagnitude -> ControllerUnsignedFloat.RightStickMagnitude;
            case DpadX -> ControllerUnsignedFloat.DpadX;
            case DpadY -> ControllerUnsignedFloat.DpadY;
            case DpadMagnitude -> ControllerUnsignedFloat.DpadMagnitude;
            case TriggerMagnitude -> ControllerUnsignedFloat.TriggerMagnitude;
            default -> ControllerUnsignedFloat.Unknown;
        };
    }

    @Override
    public ControllerSignedFloat asSignedFloat() {
        return this;
    }

    @Override
    public Pair<ControllerUnsignedFloat, Float> asUnsignedFloat(Float value) {
        return new Pair<>(asUnsignedFloat(), value * 2 - 1);
    }

    @Override
    public Pair<ControllerSignedFloat, Float> asSignedFloat(Float value) {
        return new Pair<>(this, value);
    }

    @Override
    public Pair<ControllerVec2, Vector2> asVec2(Float value, Vector2 result) {
        Pair<ControllerSignedFloat, Float> signedFloat = asSignedFloat(value);
        return signedFloat.getFirst().asVec2(signedFloat.getSecond(), result);
    }

    @Override
    public ControllerVec2 asVec2() {
        return switch (this) {
            case LeftStickX, LeftStickY, LeftStickMagnitude -> ControllerVec2.LeftStick;
            case RightStickX, RightStickY, RightStickMagnitude -> ControllerVec2.RightStick;
            case DpadX, DpadY, DpadMagnitude -> ControllerVec2.Dpad;
            case TriggerMagnitude -> ControllerVec2.Triggers;
            default -> ControllerVec2.Unknown;
        };
    }

    @Override
    public ControllerIcon getIcon() {
        return switch (this) {
            case LeftTrigger, TriggerMagnitude -> ControllerIcon.LeftTrigger;
            case RightTrigger -> ControllerIcon.RightTrigger;
            case LeftStickX -> ControllerIcon.LeftJoyStickX;
            case LeftStickY -> ControllerIcon.LeftJoyStickY;
            case RightStickX -> ControllerIcon.RightJoyStickX;
            case RightStickY -> ControllerIcon.RightJoyStickY;
            case LeftStickMagnitude -> ControllerIcon.LeftJoyStickMove;
            case RightStickMagnitude -> ControllerIcon.RightJoyStickMove;
            case DpadX -> ControllerIcon.DpadLeftRight;
            case DpadY -> ControllerIcon.DpadUpDown;
            case DpadMagnitude -> ControllerIcon.Dpad;
            default -> ControllerIcon.AnyJoyStick;
        };
    }

    public @MagicConstant(valuesFromClass = SDL_GameControllerAxis.class) int sdlAxis() {
        return switch (this) {
            case LeftTrigger -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERLEFT;
            case RightTrigger -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_TRIGGERRIGHT;
            case LeftStickX -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTX;
            case LeftStickY -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_LEFTY;
            case RightStickX -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTX;
            case RightStickY -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_RIGHTY;
            default -> SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_INVALID;
        };
    }
}
