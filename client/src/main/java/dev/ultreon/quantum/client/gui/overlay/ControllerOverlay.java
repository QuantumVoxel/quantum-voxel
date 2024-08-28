package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.input.controller.ControllerContext;
import dev.ultreon.quantum.client.input.controller.ControllerInput;
import dev.ultreon.quantum.client.input.controller.ControllerMapping;

public class ControllerOverlay extends Overlay {
    private final ControllerInput input = client.controllerInput;

    public void render(Renderer gfx, float deltaTime) {
        ControllerContext ctx = ControllerContext.get();

        if (ctx == null) return;
        if (!input.isAvailable()) return;

        Iterable<ControllerMapping<?>> mappings = ctx.mappings.getAllMappings();

        int leftY = 20 + ctx.getYOffset();
        int rightY = 20 + ctx.getYOffset();

        for (ControllerMapping<?> mapping : mappings) {
            if (!mapping.isVisible()) continue;

            ControllerMapping.Side side = mapping.getSide();
            int x = side == ControllerMapping.Side.LEFT ? 4 + ctx.getLeftXOffset() : width - 24 - ctx.getRightXOffset();
            int y = height - (side == ControllerMapping.Side.LEFT ? leftY : rightY);
            mapping.getAction().getMapping().getIcon().render(gfx, x, y);

            if (side == ControllerMapping.Side.LEFT) {
                gfx.textLeft(mapping.getName(), 28 + ctx.getLeftXOffset(), height - leftY + 4);

                leftY += 20;
            } else {
                int textRightX = width - 28 - client.font.width(mapping.getName());
                gfx.textLeft(mapping.getName(), textRightX - ctx.getRightXOffset(), height - rightY + 4);

                rightY += 20;
            }
        }
    }
}
