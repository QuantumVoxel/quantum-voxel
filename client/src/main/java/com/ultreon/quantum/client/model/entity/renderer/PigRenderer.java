package com.ultreon.quantum.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.model.EntityModelInstance;
import com.ultreon.quantum.client.model.WorldRenderContext;
import com.ultreon.quantum.client.model.entity.EntityModel;
import com.ultreon.quantum.client.render.EntityTextures;
import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PigRenderer extends LivingEntityRenderer<@NotNull Pig> {
    public PigRenderer(EntityModel<@NotNull Pig> pigModel, @Nullable Model model) {
        super(pigModel, model);
    }

    @Override
    public void animate(EntityModelInstance<@NotNull Pig> instance, WorldRenderContext<@NotNull Pig> context) {
        Pig entity = instance.getEntity();
        Entity.Pose pose = entity.getPose();

        switch (pose) {
            case IDLE -> instance.getModel().queue("animation.model.idle", -1, 1f, null, 0f);
            case WALKING -> {
                // TODO: Add walking animation
            }
            default -> QuantumClient.LOGGER.warn("Unknown pose {} for entity {}", pose, entity.getId());
        }
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}
