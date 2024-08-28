package dev.ultreon.quantum.client.input.controller;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.gui.Notification;
import dev.ultreon.quantum.client.gui.icon.ControllerIcon;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.gui.screens.container.InventoryScreen;
import dev.ultreon.quantum.client.input.GameInput;
import dev.ultreon.quantum.client.input.controller.context.InGameControllerContext;
import dev.ultreon.quantum.client.input.controller.gui.VirtualKeyboardEditCallback;
import dev.ultreon.quantum.client.input.controller.gui.VirtualKeyboardSubmitCallback;
import dev.ultreon.quantum.client.input.controller.keyboard.KeyboardLayout;
import dev.ultreon.quantum.client.input.controller.keyboard.KeyboardLayouts;
import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.util.PlayerView;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.vec.BlockVec;
import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis;
import io.github.libsdl4j.api.gamecontroller.SDL_GameControllerButton;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.*;
import static io.github.libsdl4j.api.event.SdlEventsConst.SDL_PRESSED;
import static io.github.libsdl4j.api.gamecontroller.SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_MAX;
import static io.github.libsdl4j.api.gamecontroller.SdlGamecontroller.*;

public class ControllerInput extends GameInput {
    private SDL_GameController sdlController;
    private final BitSet pressedButtons = new BitSet();
    private final float[] oldAxes = new float[ControllerSignedFloat.values().length];
    private final float[] axes = new float[ControllerSignedFloat.values().length];
    private Controller controller;
    private final Vector2 leftStick = new Vector2();
    private String virtualKeyboardValue;
    private boolean virtualKeyboardOpen;
    private Vector2 tmp;
    private final KeyboardLayout layout = KeyboardLayouts.QWERTY;
    private InterceptCallback interceptCallback;
    private CountInvalidation interceptInvalidation;

    public ControllerInput(QuantumClient client, Camera camera) {
        super(client, camera);

        SDL_Init(SDL_INIT_GAMECONTROLLER | SDL_INIT_EVENTS | SDL_INIT_HAPTIC);
    }

    private boolean pollEvents() {
        SDL_GameControllerUpdate();

        ControllerBoolean.pollAll();

        if (this.sdlController == null) {
            this.setController(0);
            if (this.sdlController == null)
                return true;
        }

        if (!SDL_GameControllerGetAttached(sdlController)) {
            unsetController();
            return true;
        }

        for (@MagicConstant(valuesFromClass = SDL_GameControllerButton.class) int idx = SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_A; idx < SDL_GameControllerButton.SDL_CONTROLLER_BUTTON_MAX; idx++) {
            boolean pressed = SDL_GameControllerGetButton(this.sdlController, idx) == SDL_PRESSED;
            this.pressedButtons.set(idx, pressed);

            if (pressed) {
                GameInput.switchTo(this);
            }
        }

        for (int i = 0; i < ControllerSignedFloat.values().length; i++) {
            ControllerSignedFloat axis = ControllerSignedFloat.values()[i];
            Float axisValue = getAxis0(axis);
            if (axisValue == null) axisValue = 0.0F;
            this.oldAxes[i] = this.axes[i];
            this.axes[i] = axisValue;

            if (axisValue != 0)
                GameInput.switchTo(this);
        }
        return false;
    }

    public static boolean isControllerConnected() {
        return QuantumClient.get().controllerInput.isConnected();
    }

    public void closeVirtualKeyboard() {
        this.virtualKeyboardValue = "";
        this.virtualKeyboardOpen = false;
        this.client.virtualKeyboard.close();
    }

    public void openVirtualKeyboard(VirtualKeyboardEditCallback callback) {
        openVirtualKeyboard("", callback);
    }

    public void openVirtualKeyboard(@NotNull String value, VirtualKeyboardEditCallback callback) {
        if (!ClientConfig.enableVirtualKeyboard) return;

        this.virtualKeyboardValue = value;
        this.virtualKeyboardOpen = true;

        this.client.virtualKeyboard.open(callback, () -> callback.onInput(client.virtualKeyboard.getScreen().getInput()));
    }

    public void openVirtualKeyboard(@NotNull String value, VirtualKeyboardEditCallback callback, VirtualKeyboardSubmitCallback submitCallback) {
        this.virtualKeyboardValue = value;
        this.virtualKeyboardOpen = true;

        this.client.virtualKeyboard.open(callback, submitCallback);
    }

    public void handleVirtualKeyboardClosed(String value) {
        this.virtualKeyboardValue = value;
        this.virtualKeyboardOpen = false;
    }

    public @NotNull String getVirtualKeyboardValue() {
        return virtualKeyboardValue;
    }

    public boolean isVirtualKeyboardOpen() {
        return virtualKeyboardOpen;
    }

    public boolean isJoystickRight() {
        return leftStick.x > 0 && isXAxis();
    }

    public boolean isJoystickDown() {
        return leftStick.y > 0 && isYAxis();
    }

    public boolean isJoystickLeft() {
        return leftStick.x < 0 && isXAxis();
    }

    public boolean isJoystickUp() {
        return leftStick.y < 0 && isYAxis();
    }

    private boolean isYAxis() {
        return Math.abs(leftStick.x) <= Math.abs(leftStick.y);
    }

    private boolean isXAxis() {
        return Math.abs(leftStick.x) > Math.abs(leftStick.y);
    }

    float getAxis(ControllerSignedFloat controllerAxis) {
        Float v = getAxis0(controllerAxis);
        if (v == null) return 0f;

        if (GameInput.getCurrent() instanceof ControllerInput) {
            return -v;
        }

        return 0;
    }

    @Override
    public void update(float deltaTime) {
        if (pollEvents()) return;

        ControllerContext context = ControllerContext.get();
        LocalPlayer player = client.player;
        if (player != null && context instanceof InGameControllerContext inGameControllerContext) {
            this.updatePlayer(deltaTime, player, inGameControllerContext);
        }
    }

    private void updatePlayer(float deltaTime, Player player, InGameControllerContext context) {
        if (context.useItem.getAction().isPressed()) {
            useItem(player, client.world, client.cursor, context.useItem.getAction().getValue());
        } else if (context.jump.getAction().isPressed()) {
            if (player.isInWater()) client.playerInput.up = true;
            else if (player.onGround) player.jump();
        } else if (context.changeItemLeft.getAction().isJustPressed()) {
            int selected = player.selected - 1;
            if (selected < 0) selected = 8;
            player.selected = selected;
        } else if (context.changeItemRight.getAction().isJustPressed()) {
            int selected = player.selected + 1;
            if (selected > 8) selected = 0;
            player.selected = selected;
        } else if (context.dropItem.getAction().isPressed()) {
            player.dropItem();
        } else if (context.changePerspective.getAction().isPressed()) {
            client.setPlayerView(switch (client.getPlayerView()) {
                case FIRST_PERSON -> PlayerView.THIRD_PERSON;
                case THIRD_PERSON -> PlayerView.THIRD_PERSON_FRONT;
                case THIRD_PERSON_FRONT -> PlayerView.FIRST_PERSON;
            });
        } else if (context.openChat.getAction().isPressed()) {
            client.showScreen(new ChatScreen());
        } else if (context.openInventory.getAction().isPressed()) {
            client.showScreen(new InventoryScreen(player.inventory, player.inventory.getTitle()));
        } else if (context.destroyBlock.getAction().isPressed()) {
            if (player.abilities.instaMine) {
                BlockHit cursor = client.cursor;
                if (cursor != null)
                    this.client.connection.send(new C2SBlockBreakPacket(new BlockVec(cursor.getBlockVec())));

                this.client.stopBreaking();
                return;
            }

            this.client.startBreaking();
        } else if (context.destroyBlock.getAction().isJustReleased()) {
            if (player.abilities.instaMine) return;

            this.client.stopBreaking();
        } else if (context.placeBlock.getAction().isPressed()) {
            this.client.stopBreaking();

            BlockHit cursor = client.cursor;
            if (cursor == null) return;
            this.useItem(player, client.world, cursor);
        } else if (context.moveHead.getAction().isPressed()) {
            Vector2 vector2 = context.moveHead.getAction().get2DValue();
            player.rotateHead(vector2.x * 20F * deltaTime, vector2.y * 20F * deltaTime);
        } else {
            // Unknown
        }

        updateControllerMove(deltaTime, player, context.move.getAction().getAxisValue());
    }

    private void updateControllerMove(float deltaTime, Player player, float speed) {
        Vec3d tmp = new Vec3d();
        Vector3 velocity = this.client.playerInput.getVelocity();
        this.vel.set(velocity.x, velocity.y, velocity.z);

        // Water movement
        if (player.isInWater() && this.client.playerInput.up) {
            tmp.set(0, 1, 0).nor().mul(speed);
            this.vel.add(tmp);


            // If not affected by fluid, reset the flag and set the vertical velocity
            if (player.wasInFluid && !player.isAffectedByFluid()) {
                player.wasInFluid = false;
                player.velocityY = 0.3;
            }
        }

        // Flight movement
        if (player.isFlying() && this.client.playerInput.up) {
            tmp.set(0, 1, 0).nor().mul(speed);
            this.vel.add(tmp);
        }

        if (player.isFlying() && this.client.playerInput.down) {
            tmp.set(0, 1, 0).nor().mul(-speed);
            this.vel.add(tmp);
        }

        this.vel.x *= deltaTime * QuantumServer.TPS;
        this.vel.y *= deltaTime * QuantumServer.TPS;
        this.vel.z *= deltaTime * QuantumServer.TPS;

        player.setVelocity(player.getVelocity().add(this.vel));
    }

    @Override
    public void dispose() {
        SDL_Quit();
    }

    private @Nullable Float getAxis0(ControllerSignedFloat controllerAxis) {
        @MagicConstant(valuesFromClass = SDL_GameControllerAxis.class) int axis = controllerAxis.sdlAxis();
        if (axis == SDL_GameControllerAxis.SDL_CONTROLLER_AXIS_INVALID) return null;
        float v = SDL_GameControllerGetAxis(sdlController, axis) / 32767f;

        float deadZone = ControllerConfig.get().axisDeadZone;
        int signum = v > 0 ? 1 : -1;
        v = Math.abs(v);
        if (v < deadZone) {
            v = Math.max(0, (v - deadZone) / (1 - deadZone)) * signum;
        } else {
            v *= signum;
        }

        if (v == 0) return 0f;

        return v;
    }

    private float getOldAxis(ControllerSignedFloat controllerAxis) {
        return oldAxes[controllerAxis.sdlAxis()];
    }

    @SuppressWarnings("SameParameterValue")
    private void setController(int deviceIndex) {
        this.sdlController = SDL_GameControllerOpen(deviceIndex);
        if (sdlController == null) return;

        short productId = SDL_GameControllerGetProduct(sdlController);
        short vendorId = SDL_GameControllerGetVendor(sdlController);
        String name = SDL_GameControllerName(sdlController);
        String mapping = SDL_GameControllerMapping(sdlController);

        this.controller = new Controller(sdlController, deviceIndex, productId, vendorId, name, mapping);

        ControllerEvent.CONTROLLER_CONNECTED.factory().onConnectionStatus(this.controller);
        client.notifications.add(Notification.builder(TextObject.translation("quantum.message.controller_connected.title"), TextObject.translation("quantum.message.controller_connected.text", name)).duration(Duration.ofSeconds(5)).build());

        CommonConstants.LOGGER.info("Controller {} connected", name);
    }

    private void unsetController() {
        if (0 != this.controller.deviceIndex()) return;

        QuantumClient.get().notifications.add(Notification.builder(TextObject.translation("quantum.message.controller_disconnected.title"), TextObject.translation("quantum.message.controller_disconnected.text")).subText(this.controller.name()).duration(Duration.ofSeconds(5)).icon(ControllerIcon.AnyJoyStick).build());

        this.sdlController = null;
        this.controller = null;

        ControllerEvent.CONTROLLER_DISCONNECTED.factory().onConnectionStatus(this.controller);
        GameInput.switchToFallback();

        CommonConstants.LOGGER.info("Controller disconnected");
    }

    public @Nullable Controller getController() {
        return controller;
    }

    public @Nullable SDL_GameController getSDLController() {
        return sdlController;
    }

    public boolean isButtonPressed(ControllerBoolean button) {
        return button.isPressed();
    }

    public boolean isButtonJustPressed(ControllerBoolean button) {
        return button.isJustPressed();
    }

    public boolean isButtonJustReleased(ControllerBoolean button) {
        return button.isJustReleased();
    }

    public Vector2 getJoystick(ControllerVec2 joystick) {
        return joystick.get(this.tmp);
    }

    public float getTrigger(ControllerUnsignedFloat trigger) {
        return trigger.getValue();
    }

    boolean isButtonPressed0(ControllerBoolean button) {
        int idx = button.sdlButton();
        boolean pressed = SDL_GameControllerGetButton(sdlController, idx) == SDL_PRESSED;

        if (GameInput.getCurrent() instanceof ControllerInput) return pressed;

        return false;
    }

    public boolean isConnected() {
        return controller != null && SDL_GameControllerGetAttached(controller.sdlController());
    }

    public boolean isAvailable() {
        return isConnected() && GameInput.getCurrent() instanceof ControllerInput;
    }

    public KeyboardLayout getLayout() {
        return layout;
    }

    public void interceptInputOnce(InterceptCallback callback) {
        interceptCallback = callback;
        interceptInvalidation = new CountInvalidation(1);
    }

    @FunctionalInterface
    public interface InterceptCallback {
        void onIntercept(EventObject<?, ?> type);
    }

    public static class EventType<T> {
        public static final EventType<ControllerSignedFloat> AXIS = new EventType<>(ControllerSignedFloat.class);
        public static final EventType<ControllerBoolean> BUTTON = new EventType<>(ControllerBoolean.class);
        public static final EventType<ControllerVec2> JOYSTICK = new EventType<>(ControllerVec2.class);
        public static final EventType<ControllerUnsignedFloat> TRIGGER = new EventType<>(ControllerUnsignedFloat.class);

        private final Class<T> type;

        private EventType(Class<T> type) {
            this.type = type;
        }

        @ApiStatus.Internal
        @SuppressWarnings("unchecked")
        public static <T extends ControllerInterDynamic<?>> EventType<T> get(Class<?> aClass) {
            if (aClass == ControllerAction.Button.class) return (EventType<T>) BUTTON;
            if (aClass == ControllerAction.Axis.class) return (EventType<T>) AXIS;

            throw new IllegalArgumentException("Invalid type: " + aClass);
        }

        public Class<T> getType() {
            return type;
        }
    }

    public record EventObject<V, T extends Enum<T> & ControllerInterDynamic<V>>(EventType<? extends T> type, T mapping,
                                                                                V value) {

        public static EventObject<Boolean, ControllerBoolean> of(ControllerBoolean controllerButton, boolean value) {
            return new EventObject<>(EventType.BUTTON, controllerButton, value);
        }

        public static EventObject<Float, ControllerSignedFloat> of(ControllerSignedFloat controllerAxis, float value) {
            return new EventObject<>(EventType.AXIS, controllerAxis, value);
        }

        public static EventObject<Vector2, ControllerVec2> of(ControllerVec2 controllerJoystick, Vector2 value) {
            return new EventObject<>(EventType.JOYSTICK, controllerJoystick, value);
        }

        public static EventObject<Float, ControllerUnsignedFloat> of(ControllerUnsignedFloat controllerTrigger, float value) {
            return new EventObject<>(EventType.TRIGGER, controllerTrigger, value);
        }
    }
}
