package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.world.ClientWorld;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.events.ItemEvents;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.UseItemContext;
import dev.ultreon.quantum.network.packets.c2s.C2SItemUsePacket;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.Hit;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.UseResult;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("GDXJavaStaticResource")
public abstract class GameInput implements Disposable {
    private static long lastSwitch;

    protected final QuantumClient client;
    protected final Camera camera;

    private static @Nullable GameInput current = null;

    protected long nextBreak;
    protected long itemUse;
    protected boolean breaking;
    protected boolean using;
    protected final Vec3d vel = new Vec3d();
    @Nullable
    protected Hit hit;
    private long itemUseCooldown;

    protected GameInput(QuantumClient client, Camera camera) {
        this.client = client;
        this.camera = camera;
    }

    public static @NotNull GameInput getCurrent() {
        final GameInput cur = current;
        if (cur != null) {
            return cur;
        }

        return current = getDefault();
    }

    private static void setCurrent(@NotNull GameInput current) {
        GameInput.current = current;
    }

    protected static void switchTo(GameInput controllerInput) {
        if (lastSwitch + 1000 > System.currentTimeMillis()) return;

        if (current != null) {
            current.switchOut();
        }
        lastSwitch = System.currentTimeMillis();
        setCurrent(controllerInput);
    }

    protected void switchOut() {

    }

    protected static void switchToFallback() {
        lastSwitch = System.currentTimeMillis();
        setCurrent(getDefault());
        Gdx.app.log("GameInput", "Switching to fallback input");
    }

    private static GameInput getDefault() {
        QuantumClient client = QuantumClient.get();
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) || GamePlatform.get().isMobile()) {
            return client.touchInput;
        } else if (Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard) && !GamePlatform.get().isMobile()) {
            return client.keyAndMouseInput;
        } else {
            Gdx.app.log("GameInput", "No input devices found, using fallback input");
            return client.keyAndMouseInput;
        }
    }

    public boolean isActive() {
        return current == this;
    }

    @ApiStatus.NonExtendable
    public final void update() {
        if (getCurrent() != this) return;

        float deltaTime = Gdx.graphics.getDeltaTime();
        this.update(deltaTime);

        LocalPlayer player = this.client.player;
    }

    public final void tick() {
        if (getCurrent() != this) return;

        LocalPlayer player = this.client.player;
        if (player != null) {
            PlayerInput input = this.client.playerInput;
            input.tick(player, (float) player.getSpeed());
        }
    }

    public abstract void update(float deltaTime);

    public UseResult useItem(Player player, @Nullable ClientWorld world, Hit hit) {
        return useItem(player, world, hit, 1F);
    }

    public UseResult useItem(Player player, @Nullable ClientWorld world, Hit hit, float amount) {
        if (this.itemUseCooldown > System.currentTimeMillis())
            return UseResult.DENY;

        UseResult useResult = useItem0(player, world, hit, amount);
        this.itemUseCooldown = System.currentTimeMillis() + 200;

        return useResult;
    }

    private UseResult useItem0(Player player, @Nullable ClientWorld world, Hit hit, float amount) {
        if (!(hit instanceof BlockHit)) return UseResult.DENY;

        ItemStack stack = player.getSelectedItem();
        UseItemContext context = new UseItemContext(world, player, hit, stack, amount);
        Item item = stack.getItem();
        ItemEvents.USE.factory().onUseItem(item, context);
        this.client.connection.send(new C2SItemUsePacket((BlockHit) hit));

        Hit result = context.result();
        if (result == null)
            return UseResult.SKIP;

        if (hit instanceof BlockHit) {
            BlockHit blockHitResult = (BlockHit) hit;
            Block block = blockHitResult.getBlock();
            if (block != null && !block.isAir()) {
                UseResult blockResult = block.use(context.world(), context.player(), stack.getItem(), new BlockVec(result.getBlockVec()));

                if (blockResult == UseResult.DENY || blockResult == UseResult.ALLOW)
                    return blockResult;
            }
        }

        return stack.getItem().use(context);
    }

    public static boolean cancelVibration() {
        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.cancelVibration();
        return true;
    }

    public static boolean startVibration(int duration, float strength) {
        if (!ClientConfiguration.vibration.getValue()) return false;

        Controller current = Controllers.getCurrent();
        if (current == null) return false;
        current.startVibration(duration, Mth.clamp(strength, 0.0F, 1.0F));
        return true;
    }

    @Override
    public void dispose() {

    }

    public abstract String getName();
}
