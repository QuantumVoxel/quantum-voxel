package dev.ultreon.quantum.client.gui.screens.container;

import dev.ultreon.quantum.block.entity.BlastFurnaceBlockEntity;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.BlastFurnaceMenu;
import dev.ultreon.quantum.menu.BlockContainerMenu;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.container.FuelRegistry;
import lombok.Getter;

import java.util.List;

@Getter
public class BlastFurnaceScreen extends ContainerScreen {
    private static final int CONTAINER_SIZE = 40;
    private static final NamespaceID BACKGROUND = QuantumClient.id("textures/gui/container/blast_furnace.png");
    private final BlastFurnaceMenu menu;

    public BlastFurnaceScreen(BlastFurnaceMenu menu, TextObject title) {
        super(menu, title, BlastFurnaceScreen.CONTAINER_SIZE);
        this.menu = menu;
    }

    @Override
    public int backgroundWidth() {
        return 181;
    }

    @Override
    public int backgroundHeight() {
        return 186;
    }

    @Override
    public NamespaceID getBackground() {
        return BlastFurnaceScreen.BACKGROUND;
    }

    @Override
    public void setup(List<ItemStack> items) {
        this.menu.setupClient(items);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.textLeft(TextObject.translation("quantum.container.blast_furnace.title"), left() + 10, top() + 4);
        renderer.textLeft(TextObject.translation("quantum.container.inventory.title"), left() + 10, top() + 74);

        float burnPercent = burnPercent();
        float progress = progress();
        renderer.blit(BACKGROUND, left() + 55, top() + 27 + (14 - (int) (14 * burnPercent)), 18,  (int) (14 * burnPercent), 183, (int) (1 + 14 - (14 * burnPercent)), 18, (int) (14 * burnPercent));
        renderer.blit(BACKGROUND, left() + 82, top() + 26, (int) (20 * progress), 13, 183, 16, (int) (20 * progress), 13);
    }

    private float burnPercent() {
        if (menu instanceof BlockContainerMenu containerMenu
                && containerMenu.getBlockEntity() instanceof BlastFurnaceBlockEntity blockEntity)
            return (float) blockEntity.getBurnTime() / blockEntity.getMaxBurnTime();
        return 0;
    }

    private float progress() {
        if (menu instanceof BlockContainerMenu containerMenu
                && containerMenu.getBlockEntity() instanceof BlastFurnaceBlockEntity blockEntity)
            return (float) blockEntity.getCookTime() / blockEntity.getMaxCookTime();
        return 0;
    }
}
