package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.input.controller.ControllerContext;
import dev.ultreon.quantum.client.input.controller.ControllerInput;
import dev.ultreon.quantum.client.input.controller.context.InGameControllerContext;
import dev.ultreon.quantum.client.input.key.KeyBinds;
import dev.ultreon.quantum.client.util.Utils;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.Vec3d;
import org.jetbrains.annotations.NotNull;

public class PlayerInput {
    public boolean forward;
    public boolean backward;
    public boolean strafeLeft;
    public boolean strafeRight;
    public boolean up;
    public boolean down;
    public float moveX;
    public float moveY;
    private final Vector3 vel = new Vector3();
    private final Vector3 tmp = new Vector3();
    private int flyCountdown = 0;

    public void tick(@NotNull Player player, float speed) {
        vel.set(0, 0, 0);

        moveX = 0;
        moveY = 0;

        forward = KeyBinds.walkForwardsKey.isPressed() && Gdx.input.isCursorCatched();
        backward = KeyBinds.walkBackwardsKey.isPressed() && Gdx.input.isCursorCatched();
        strafeLeft = KeyBinds.walkLeftKey.isPressed() && Gdx.input.isCursorCatched();
        strafeRight = KeyBinds.walkRightKey.isPressed() && Gdx.input.isCursorCatched();
        up = KeyBinds.jumpKey.isPressed() && Gdx.input.isCursorCatched();
        down = KeyBinds.crouchKey.isPressed() && Gdx.input.isCursorCatched();
        player.setRunning(KeyBinds.runningKey.isPressed() && Gdx.input.isCursorCatched());

        toggleFlight(player);

        move();
        controllerMove();

        rotate(player);

        tmp.set(-moveX, 0, moveY).nor().scl(speed).rotate(player.xHeadRot, 0, 1, 0);
        vel.set(tmp);
    }

    private void toggleFlight(@NotNull Player player) {
        if (flyCountdown <= 0) {
            if (KeyBinds.jumpKey.isJustPressed() && player.isAllowFlight())
                flyCountdown = ClientConfig.doublePressDelay;

            return;
        }
        flyCountdown--;

        if (!KeyBinds.jumpKey.isJustPressed() || !player.isAllowFlight() || player.isSpectator())
            return;

        player.setFlying(!player.isFlying());
        flyCountdown = 0;
    }

    private void rotate(@NotNull Player player) {
        if (moveX > 0)
            player.xRot = Math.max(player.xRot - 45 / (player.xHeadRot - player.xRot + 50), player.xRot - 90);
        else if (moveX < -0)
            player.xRot = Math.min(player.xRot + 45 / (player.xRot - player.xHeadRot + 50), player.xRot + 90);
        else if (moveY != 0 && player.xRot > player.xHeadRot)
            player.xRot = Math.max(player.xRot - (45 / (player.xRot - player.xHeadRot)), player.xHeadRot);
        else if (moveY != 0 && player.xRot < player.xHeadRot)
            player.xRot = Math.min(player.xRot + (45 / (player.xHeadRot - player.xRot)), player.xHeadRot);
    }

    private void controllerMove() {
        if (!ControllerInput.isControllerConnected() || moveX != 0 || moveY != 0)
            return;
        if (!(GameInput.getCurrent() instanceof ControllerInput))
            return;

        if (ControllerContext.get() instanceof InGameControllerContext context) {
            Vector2 joystick = context.move.getAction().get2DValue();

            if (joystick == null)
                return;

            moveX = -joystick.x;
            moveY = joystick.y;
        }
    }

    private void move() {
        if (!(GameInput.getCurrent() instanceof KeyAndMouseInput)) return;
        if (!forward && !backward && !strafeLeft && !strafeRight)
            return;

        if (forward)
            moveY += 1;

        if (backward)
            moveY -= 1;

        if (strafeLeft)
            moveX -= 1;

        if (strafeRight)
            moveX += 1;
    }

    @Deprecated
    public Vec3d getVel() {
        return Utils.toCoreLibs(vel);
    }

    public Vector3 getVelocity() {
        return vel;
    }

    public boolean isWalking() {
        return forward || backward || strafeLeft || strafeRight;
    }
}
