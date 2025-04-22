package dev.ultreon.quantum.client.model.entity.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.EntityModelInstance;
import dev.ultreon.quantum.client.model.WorldRenderContext;
import dev.ultreon.quantum.client.model.entity.PlayerModel;
import dev.ultreon.quantum.client.player.ClientPlayer;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.render.EntityTextures;
import dev.ultreon.quantum.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRenderer extends QVModelEntityRenderer<@NotNull Player> {
    public PlayerRenderer(PlayerModel<@NotNull Player> playerModel, @Nullable Model model) {
        super(playerModel, model);
    }

    @Override
    public void animate(EntityModelInstance<@NotNull Player> instance, WorldRenderContext<@NotNull Player> context) {
        Player player = instance.getEntity();
        if (!(player instanceof ClientPlayer)) return;
        ClientPlayer clientPlayer = (ClientPlayer) player;

        LocalPlayer localPlayer = this.client.player;
        if (localPlayer == null) return;

        float xRot = clientPlayer.xRot;
        float yRot = clientPlayer.yRot;

        try {
            instance.getNode("left_leg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
            instance.getNode("right_leg").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (-clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
            instance.getNode("left_pants").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
            instance.getNode("right_pants").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (-clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
            instance.getNode("left_arm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (-clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
            instance.getNode("right_arm").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
            instance.getNode("left_sleve").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (-clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));
            instance.getNode("right_sleve").rotation.idt().setFromMatrix(this.tmp.idt().rotate(Vector3.X, (float) (clientPlayer.walkAnim * 3000 * clientPlayer.getSpeed())));

        } catch (Exception ignored) {
            // ignore
        }

        float duration = 0.15f;
        var walkAnim = clientPlayer.walkAnim;
        float delta = Gdx.graphics.getDeltaTime();

        if (clientPlayer.isWalking()) clientPlayer.walking = true;
        if (!clientPlayer.walking) clientPlayer.walkAnim = 0;
        else QVModelEntityRenderer.updateWalkAnim(clientPlayer, walkAnim, delta, duration);

        float bopDuration = 3.4f;
        var bop = clientPlayer.bop;
        bop -= clientPlayer.inverseBop ? delta : -delta;

        if (bop > bopDuration) {
            float overflow = bopDuration - bop;
            bop = bopDuration - overflow;
            clientPlayer.inverseBop = true;
        } else if (bop < -bopDuration) {
            float overflow = bopDuration + bop;
            bop = -bopDuration - overflow;
            clientPlayer.inverseBop = false;
        }

        clientPlayer.bop = bop;

        float bopZDuration = 2.7f;
        var bopZ = clientPlayer.bopZ;
        bopZ -= clientPlayer.inverseBopZ ? delta : -delta;

        if (bopZ > bopZDuration) {
            float overflow = bopZDuration - bopZ;
            bopZ = bopZDuration - overflow;
            clientPlayer.inverseBopZ = true;
        } else if (bopZ < -bopZDuration) {
            float overflow = bopZDuration + bopZ;
            bopZ = -bopZDuration - overflow;
            clientPlayer.inverseBopZ = false;
        }

        clientPlayer.bopZ = bopZ;

        try {
            instance.getNode("head").rotation.setFromMatrix(this.tmp.idt().rotate(Vector3.Y, player.xHeadRot - xRot).rotate(Vector3.X, yRot));
            instance.getNode("headwear").rotation.setFromMatrix(this.tmp.idt().rotate(Vector3.Y, player.xHeadRot - xRot).rotate(Vector3.X, yRot));
        } catch (Exception ignored) {
            // ignore
        }

//        EntityRenderer.tmp0.set(localPlayer.getPosition(client.partialTick));
//        EntityRenderer.tmp0.sub(player.getPosition());
        instance.rotateY(xRot - 180);

//        TextureManager textureManager = client.getTextureManager();
//        Identifier id = Identifier.parse("dynamic/player_skin/" + this.client.player.getUuid().toString().replace("-", ""));
//        if (!textureManager.isTextureLoaded(id)) {
//            Texture localSkin = client.getSkinManager().getLocalSkin();
//            if (localSkin != null) {
//                textureManager.registerTexture(id, localSkin);
//                instance.setTextures(id);
//            }
//        } else {
//            instance.setTextures(id);
//        }
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures().set(TextureAttribute.Diffuse, QuantumClient.id("textures/entity/player"));
    }
}
