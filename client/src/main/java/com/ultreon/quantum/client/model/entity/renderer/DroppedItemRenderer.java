package com.ultreon.quantum.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.init.Shaders;
import com.ultreon.quantum.client.model.EntityModelInstance;
import com.ultreon.quantum.client.model.WorldRenderContext;
import com.ultreon.quantum.client.model.entity.EntityModel;
import com.ultreon.quantum.client.model.item.ItemModel;
import com.ultreon.quantum.client.render.EntityTextures;
import com.ultreon.quantum.entity.DroppedItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DroppedItemRenderer extends EntityRenderer<@NotNull DroppedItem> {
    public DroppedItemRenderer(EntityModel<@NotNull DroppedItem> droppedItemModel, @Nullable Model model) {
        super();
    }

    @Override
    public void animate(EntityModelInstance<@NotNull DroppedItem> instance, WorldRenderContext<@NotNull DroppedItem> context) {
        DroppedItem entity = instance.getEntity();
        int age = entity.getAge();
        float rotation = age * 5f % 360;
        float translation = MathUtils.sinDeg(age % 180 * 2) / 8f;

        ItemModel userData = (ItemModel) instance.getModel().userData;
        Vector3 offset = userData.getOffset();
        Vector3 scale = userData.getScale();

        instance.rotateY(rotation);
        instance.translate(0, translation - 1, 0);
        instance.scale(-0.15f, -0.15f, -0.15f);
        instance.scale(scale.x, scale.y, scale.z);
        instance.translate(offset.x, offset.y, offset.z);
        instance.translate(0.5, 0, -0.5);
    }

    @Override
    public ModelInstance createModel(@NotNull DroppedItem entity) {
        if (entity.getStack().isEmpty()) {
            QuantumClient.LOGGER.warn("Tried to render empty item stack");
            return null;
        }

        return Objects.requireNonNull(client.itemRenderer.createModelInstance(entity.getStack()));
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}
