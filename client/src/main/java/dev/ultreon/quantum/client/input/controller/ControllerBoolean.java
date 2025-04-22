//package dev.ultreon.quantum.client.input.controller;
//
//import com.badlogic.gdx.math.Vector2;
//import dev.ultreon.libs.commons.v0.tuple.Pair;
//import dev.ultreon.quantum.client.QuantumClient;
//import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
//import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
//import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton;
//import org.intellij.lang.annotations.MagicConstant;
//
//public enum ControllerBoolean implements ControllerInterDynamic<Boolean> {
//    AnyButton,
//    A,
//    B,
//    X,
//    Y,
//    Back,
//    Start,
//    Guide,
//    AnyJoyStick,
//    LeftStickAny,
//    RightStickAny,
//    LeftStickUsed,
//    RightStickUsed,
//    LeftStickX,
//    LeftStickY,
//    RightStickX,
//    RightStickY,
//    LeftStickLeft,
//    LeftStickRight,
//    LeftStickUp,
//    LeftStickDown,
//    RightStickLeft,
//    RightStickRight,
//    RightStickUp,
//    RightStickDown,
//    Touchpad,
//    AnyDpad,
//    DpadX,
//    DpadY,
//    DpadLeft,
//    DpadRight,
//    DpadUp,
//    DpadDown,
//    DpadUsed,
//    LeftStickClick,
//    RightStickClick,
//    AnyShoulder,
//    LeftShoulder,
//    RightShoulder,
//    AnyTrigger,
//    LeftTrigger,
//    RightTrigger,
//    Unknown;
//
//    private boolean lastValue = false;
//    private boolean value = false;
//
//    public boolean getValue() {
//        return this.value;
//    }
//
//    public boolean getPrevValue() {
//        return lastValue;
//    }
//
//    @Override
//    public ControllerSignedFloat asSignedFloat() {
//        switch (this) {
//            case LeftStickX:
//                return ControllerSignedFloat.LeftStickX;
//            case LeftStickY:
//                return ControllerSignedFloat.LeftStickY;
//            case RightStickX:
//                return ControllerSignedFloat.RightStickX;
//            case RightStickY:
//                return ControllerSignedFloat.RightStickY;
//            case LeftStickUsed:
//                return ControllerSignedFloat.LeftStickMagnitude;
//            case RightStickUsed:
//                return ControllerSignedFloat.RightStickMagnitude;
//            case DpadX:
//                return ControllerSignedFloat.DpadX;
//            case DpadY:
//                return ControllerSignedFloat.DpadY;
//            case DpadUsed:
//                return ControllerSignedFloat.DpadMagnitude;
//            default:
//                return ControllerSignedFloat.Unknown;
//        }
//    }
//
//    @Override
//    public ControllerUnsignedFloat asUnsignedFloat() {
//        switch (this) {
//            case LeftStickX:
//                return ControllerUnsignedFloat.LeftStickX;
//            case LeftStickY:
//                return ControllerUnsignedFloat.LeftStickY;
//            case RightStickX:
//                return ControllerUnsignedFloat.RightStickX;
//            case RightStickY:
//                return ControllerUnsignedFloat.RightStickY;
//            case LeftStickUsed:
//                return ControllerUnsignedFloat.LeftStickMagnitude;
//            case RightStickUsed:
//                return ControllerUnsignedFloat.RightStickMagnitude;
//            case DpadX:
//                return ControllerUnsignedFloat.DpadX;
//            case DpadY:
//                return ControllerUnsignedFloat.DpadY;
//            case DpadUsed:
//                return ControllerUnsignedFloat.DpadMagnitude;
//            default:
//                return ControllerUnsignedFloat.Unknown;
//        }
//    }
//
//    @Override
//    public ControllerVec2 asVec2() {
//        switch (this) {
//            case LeftTrigger:
//            case RightTrigger:
//                return ControllerVec2.Triggers;
//            case LeftStickX:
//            case LeftStickY:
//            case LeftStickUsed:
//                return ControllerVec2.LeftStick;
//            case RightStickX:
//            case RightStickY:
//            case RightStickUsed:
//                return ControllerVec2.RightStick;
//            case DpadX:
//            case DpadY:
//            case DpadUsed:
//                return ControllerVec2.Dpad;
//            default:
//                return ControllerVec2.Unknown;
//        }
//    }
//
//    @Override
//    public Pair<ControllerSignedFloat, Float> asSignedFloat(Boolean value) {
//        switch (this) {
//            case LeftStickX:
//                return new Pair<>(ControllerSignedFloat.LeftStickX, value ? 1f : -1f);
//            case LeftStickY:
//                return new Pair<>(ControllerSignedFloat.LeftStickY, value ? 1f : -1f);
//            case RightStickX:
//                return new Pair<>(ControllerSignedFloat.RightStickX, value ? 1f : -1f);
//            case RightStickY:
//                return new Pair<>(ControllerSignedFloat.RightStickY, value ? 1f : -1f);
//            case LeftStickUsed:
//                return new Pair<>(ControllerSignedFloat.LeftStickMagnitude, value ? 1f : -1f);
//            case RightStickUsed:
//                return new Pair<>(ControllerSignedFloat.RightStickMagnitude, value ? 1f : -1f);
//            case DpadX:
//                return new Pair<>(ControllerSignedFloat.DpadX, value ? 1f : -1f);
//            case DpadY:
//                return new Pair<>(ControllerSignedFloat.DpadY, value ? 1f : -1f);
//            case DpadUsed:
//                return new Pair<>(ControllerSignedFloat.DpadMagnitude, value ? 1f : -1f);
//            default:
//                return new Pair<>(ControllerSignedFloat.Unknown, 0f);
//        }
//    }
//
//    @Override
//    public Pair<ControllerUnsignedFloat, Float> asUnsignedFloat(Boolean value) {
//        switch (this) {
//            case LeftStickX:
//                return new Pair<>(ControllerUnsignedFloat.LeftStickX, value ? 1f : 0f);
//            case LeftStickY:
//                return new Pair<>(ControllerUnsignedFloat.LeftStickY, value ? 1f : 0f);
//            case RightStickX:
//                return new Pair<>(ControllerUnsignedFloat.RightStickX, value ? 1f : 0f);
//            case RightStickY:
//                return new Pair<>(ControllerUnsignedFloat.RightStickY, value ? 1f : 0f);
//            case LeftStickUsed:
//                return new Pair<>(ControllerUnsignedFloat.LeftStickMagnitude, value ? 1f : 0f);
//            case RightStickUsed:
//                return new Pair<>(ControllerUnsignedFloat.RightStickMagnitude, value ? 1f : 0f);
//            case DpadX:
//                return new Pair<>(ControllerUnsignedFloat.DpadX, value ? 1f : 0f);
//            case DpadY:
//                return new Pair<>(ControllerUnsignedFloat.DpadY, value ? 1f : 0f);
//            case DpadUsed:
//                return new Pair<>(ControllerUnsignedFloat.DpadMagnitude, value ? 1f : 0f);
//            default:
//                return new Pair<>(ControllerUnsignedFloat.Unknown, 0f);
//        }
//    }
//
//    @Override
//    public Pair<ControllerVec2, Vector2> asVec2(Boolean value, Vector2 result) {
//        switch (this) {
//            case LeftTrigger:
//                return new Pair<>(ControllerVec2.Triggers, value ? result.set(1, 0) : result.set(-1, 0));
//            case RightTrigger:
//                return new Pair<>(ControllerVec2.Triggers, value ? result.set(0, 1) : result.set(0, -1));
//            case LeftStickX:
//                return new Pair<>(ControllerVec2.LeftStick, value ? result.set(1, 0) : result.set(-1, 0));
//            case LeftStickY:
//                return new Pair<>(ControllerVec2.LeftStick, value ? result.set(0, 1) : result.set(0, -1));
//            case RightStickX:
//                return new Pair<>(ControllerVec2.RightStick, value ? result.set(1, 0) : result.set(-1, 0));
//            case RightStickY:
//                return new Pair<>(ControllerVec2.RightStick, value ? result.set(0, 1) : result.set(0, -1));
//            case LeftStickUsed:
//                return new Pair<>(ControllerVec2.LeftStick, value ? result.set(1, 1) : result.set(-1, -1));
//            case RightStickUsed:
//                return new Pair<>(ControllerVec2.RightStick, value ? result.set(1, 1) : result.set(-1, -1));
//            case DpadX:
//                return new Pair<>(ControllerVec2.Dpad, value ? result.set(1, 0) : result.set(-1, 0));
//            case DpadY:
//                return new Pair<>(ControllerVec2.Dpad, value ? result.set(0, 1) : result.set(0, -1));
//            case DpadUsed:
//                return new Pair<>(ControllerVec2.Dpad, value ? result.set(1, 1) : result.set(-1, -1));
//            default:
//                return new Pair<>(ControllerVec2.Unknown, result.set(0, 0));
//        }
//    }
//
//    @Override
//    public Pair<ControllerBoolean, Boolean> asBoolean(Boolean value) {
//        return new Pair<>(this, value);
//    }
//
//    @Override
//    public ControllerBoolean asBoolean() {
//        return this;
//    }
//
//    public static void pollAll() {
//        for (ControllerBoolean value : values()) {
//            value.poll();
//        }
//    }
//
//    private void poll() {
//        this.lastValue = this.value;
//        switch (this) {
//            case A:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.A);
//                break;
//            case B:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.B);
//                break;
//            case X:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.X);
//                break;
//            case Y:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.Y);
//                break;
//            case Back:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.Back);
//                break;
//            case Start:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.Start);
//                break;
//            case Guide:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.Guide);
//                break;
//            case LeftStickLeft:
//                this.value = ControllerSignedFloat.LeftStickX.getValue() < 0;
//                break;
//            case LeftStickRight:
//                this.value = ControllerSignedFloat.LeftStickX.getValue() > 0;
//                break;
//            case LeftStickUp:
//                this.value = ControllerSignedFloat.LeftStickY.getValue() < 0;
//                break;
//            case LeftStickDown:
//                this.value = ControllerSignedFloat.LeftStickY.getValue() > 0;
//                break;
//            case LeftStickAny:
//                this.value = ControllerSignedFloat.LeftStickY.getValue() != 0 || ControllerSignedFloat.LeftStickX.getValue() != 0;
//                break;
//            case RightStickLeft:
//                this.value = ControllerSignedFloat.RightStickX.getValue() < 0;
//                break;
//            case RightStickRight:
//                this.value = ControllerSignedFloat.RightStickX.getValue() > 0;
//                break;
//            case RightStickUp:
//                this.value = ControllerSignedFloat.RightStickY.getValue() < 0;
//                break;
//            case RightStickDown:
//                this.value = ControllerSignedFloat.RightStickY.getValue() > 0;
//                break;
//            case RightStickAny:
//                this.value = ControllerSignedFloat.RightStickY.getValue() != 0 || ControllerSignedFloat.RightStickX.getValue() != 0;
//                break;
//            case AnyJoyStick:
//                this.value = ControllerSignedFloat.LeftStickY.getValue() != 0 || ControllerSignedFloat.LeftStickX.getValue() != 0 || ControllerUnsignedFloat.RightStickY.getValue() != 0 || ControllerUnsignedFloat.RightStickX.getValue() != 0;
//                break;
//            case LeftStickX:
//                this.value = ControllerSignedFloat.LeftStickX.getValue() != 0;
//                break;
//            case LeftStickY:
//                this.value = ControllerSignedFloat.LeftStickY.getValue() != 0;
//                break;
//            case RightStickX:
//                this.value = ControllerSignedFloat.RightStickX.getValue() != 0;
//                break;
//            case RightStickY:
//                this.value = ControllerSignedFloat.RightStickY.getValue() != 0;
//                break;
//            case LeftStickUsed:
//                this.value = ControllerUnsignedFloat.LeftStickMagnitude.getValue() != 0;
//                break;
//            case RightStickUsed:
//                this.value = ControllerUnsignedFloat.RightStickMagnitude.getValue() != 0;
//                break;
//            case Touchpad:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.Touchpad);
//                break;
//            case DpadLeft:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadLeft);
//                break;
//            case DpadRight:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadRight);
//                break;
//            case DpadUp:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadUp);
//                break;
//            case DpadDown:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadDown);
//                break;
//            case AnyDpad:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadUp) || QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadLeft) || QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadRight) || QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadDown);
//                break;
//            case LeftStickClick:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.LeftStickClick);
//                break;
//            case RightStickClick:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.RightStickClick);
//                break;
//            case LeftShoulder:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.LeftShoulder);
//                break;
//            case RightShoulder:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.RightShoulder);
//                break;
//            case AnyShoulder:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.LeftShoulder) || QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.RightShoulder);
//                break;
//            case LeftTrigger:
//                this.value = QuantumClient.get().controllerInput.getTrigger(ControllerUnsignedFloat.LeftTrigger) != 0;
//                break;
//            case RightTrigger:
//                this.value = QuantumClient.get().controllerInput.getTrigger(ControllerUnsignedFloat.RightTrigger) != 0;
//                break;
//            case AnyTrigger:
//                this.value = QuantumClient.get().controllerInput.getTrigger(ControllerUnsignedFloat.LeftTrigger) != 0 || QuantumClient.get().controllerInput.getTrigger(ControllerUnsignedFloat.RightTrigger) != 0;
//                break;
//            case DpadX:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadLeft) || QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadRight);
//                break;
//            case DpadY:
//                this.value = QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadUp) || QuantumClient.get().controllerInput.isButtonPressed0(ControllerBoolean.DpadDown);
//                break;
//            case DpadUsed:
//                this.value = ControllerSignedFloat.DpadMagnitude.getValue() != 0;
//                break;
//            default:
//                this.value = false;
//                break;
//        }
//    }
//
//    public boolean isPressed() {
//        return value;
//    }
//
//    public boolean isJustPressed() {
//        return value && !lastValue;
//    }
//
//    public boolean isJustReleased() {
//        return !value && lastValue;
//    }
//
//    public @MagicConstant(valuesFromClass = SDL_GameControllerButton.class) int sdlButton() {
//        switch (this) {
//            case A:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_A;
//            case B:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_B;
//            case X:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_X;
//            case Y:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_Y;
//            case Back:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_BACK;
//            case Start:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_START;
//            case Guide:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_GUIDE;
//            case Touchpad:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_TOUCHPAD;
//            case DpadLeft:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_LEFT;
//            case DpadRight:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_RIGHT;
//            case DpadUp:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_UP;
//            case DpadDown:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_DPAD_DOWN;
//            case LeftStickClick:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_LEFTSTICK;
//            case RightStickClick:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_RIGHTSTICK;
//            case LeftShoulder:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_LEFTSHOULDER;
//            case RightShoulder:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_RIGHTSHOULDER;
//            default:
//                return SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_INVALID;
//        }
//    }
//
//    @Override
//    public ControllerIcon getIcon() {
//        switch (this) {
//            case A:
//                return ControllerIcon.ButtonA;
//            case B:
//                return ControllerIcon.ButtonB;
//            case X:
//                return ControllerIcon.ButtonX;
//            case Y:
//                return ControllerIcon.ButtonY;
//            case AnyJoyStick:
//                return ControllerIcon.AnyJoyStick;
//            case LeftStickAny:
//                return ControllerIcon.LeftJoyStick;
//            case RightStickAny:
//                return ControllerIcon.RightJoyStick;
//            case LeftStickX:
//                return ControllerIcon.LeftJoyStickX;
//            case LeftStickY:
//                return ControllerIcon.LeftJoyStickY;
//            case RightStickX:
//                return ControllerIcon.RightJoyStickX;
//            case RightStickY:
//                return ControllerIcon.RightJoyStickY;
//            case LeftStickUsed:
//                return ControllerIcon.LeftJoyStickMove;
//            case RightStickUsed:
//                return ControllerIcon.RightJoyStickMove;
//            case DpadLeft:
//                return ControllerIcon.DpadLeft;
//            case DpadRight:
//                return ControllerIcon.DpadRight;
//            case DpadUp:
//                return ControllerIcon.DpadUp;
//            case DpadDown:
//                return ControllerIcon.DpadDown;
//            case LeftStickClick:
//                return ControllerIcon.LeftJoyStickPress;
//            case RightStickClick:
//                return ControllerIcon.RightJoyStickPress;
//            case LeftShoulder:
//                return ControllerIcon.LeftShoulder;
//            case RightShoulder:
//                return ControllerIcon.RightShoulder;
//            case LeftTrigger:
//                return ControllerIcon.LeftTrigger;
//            case RightTrigger:
//                return ControllerIcon.RightTrigger;
//            case AnyDpad:
//                return ControllerIcon.DpadAny;
//            case DpadX:
//                return ControllerIcon.DpadLeftRight;
//            case DpadY:
//                return ControllerIcon.DpadUpDown;
//            case DpadUsed:
//                return ControllerIcon.Dpad;
//            case Start:
//                return ControllerIcon.Start;
//            case Back:
//                return ControllerIcon.XboxMenu;
//            case Guide:
//                return ControllerIcon.XboxGuide;
//            case LeftStickDown:
//                return ControllerIcon.LeftJoyStickDown;
//            case LeftStickUp:
//                return ControllerIcon.LeftJoyStickUp;
//            case LeftStickLeft:
//                return ControllerIcon.LeftJoyStickLeft;
//            case LeftStickRight:
//                return ControllerIcon.LeftJoyStickRight;
//            case RightStickDown:
//                return ControllerIcon.RightJoyStickDown;
//            case RightStickUp:
//                return ControllerIcon.RightJoyStickUp;
//            case RightStickLeft:
//                return ControllerIcon.RightJoyStickLeft;
//            case RightStickRight:
//                return ControllerIcon.RightJoyStickRight;
//            default:
//                return ControllerIcon.AnyButton;
//        }
//    }
//}
