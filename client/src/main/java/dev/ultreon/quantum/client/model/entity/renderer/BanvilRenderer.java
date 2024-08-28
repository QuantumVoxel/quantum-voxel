package dev.ultreon.quantum.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.EntityModelInstance;
import dev.ultreon.quantum.client.model.QVModel;
import dev.ultreon.quantum.client.model.WorldRenderContext;
import dev.ultreon.quantum.client.model.entity.EntityModel;
import dev.ultreon.quantum.client.render.EntityTextures;
import dev.ultreon.quantum.entity.Banvil;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BanvilRenderer extends QVModelEntityRenderer<@NotNull Banvil> {
    public BanvilRenderer(EntityModel<@NotNull Banvil> pigModel, @Nullable Model model) {
        super(pigModel, model);
    }

    @Override
    public void animate(EntityModelInstance<@NotNull Banvil> instance, WorldRenderContext<@NotNull Banvil> context) {

    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}
