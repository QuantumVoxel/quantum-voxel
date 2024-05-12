package dev.ultreon.quantum.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.EntityModelInstance;
import dev.ultreon.quantum.client.model.WorldRenderContext;
import dev.ultreon.quantum.client.model.entity.EntityModel;
import dev.ultreon.quantum.client.render.EntityTextures;
import dev.ultreon.quantum.entity.Something;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SomethingRenderer extends LivingEntityRenderer<@NotNull Something> {
    public SomethingRenderer(EntityModel<@NotNull Something> droppedItemModel, @Nullable Model model) {
        super(droppedItemModel, model);
    }

    @Override
    public void animate(EntityModelInstance<@NotNull Something> instance, WorldRenderContext<@NotNull Something> context) {
        Node bone = instance.getNode("bone");
        if (bone != null) {
//            bone.rotation.add(new Quaternion().setFromAxis(Vector3.Y, Gdx.graphics.getDeltaTime()));
        } else {
            QuantumClient.LOGGER.warn("Bone not found in model for entity %s", instance.getEntity().getId());
        }
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}
