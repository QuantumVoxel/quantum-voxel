package com.ultreon.quantum.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.model.EntityModelInstance;
import com.ultreon.quantum.client.model.WorldRenderContext;
import com.ultreon.quantum.client.model.entity.EntityModel;
import com.ultreon.quantum.client.render.EntityTextures;
import com.ultreon.quantum.entity.Something;
import org.jetbrains.annotations.NotNull;

public class SomethingRenderer extends LivingEntityRenderer<@NotNull Something> {
    public SomethingRenderer(EntityModel<@NotNull Something> droppedItemModel, Model model) {
        super(droppedItemModel, model);
    }

    @Override
    public void animate(EntityModelInstance<@NotNull Something> instance, WorldRenderContext<@NotNull Something> context) {
        Node bone = instance.getNode("bone");
        if (bone != null) {
//            bone.rotation.add(new Quaternion().setFromAxis(Vector3.Y, Gdx.graphics.getDeltaTime()));
        } else {
            QuantumClient.LOGGER.warn("Bone not found in model for entity {}", instance.getEntity().getId());
        }
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}
