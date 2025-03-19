package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class ChunkLoadScreen extends Screen {
    private final BooleanSupplier shouldClose;
    private Label description;

    public ChunkLoadScreen(BooleanSupplier shouldClose) {
        super(TextObject.translation("quantum.screen.chunk_load"));
        this.shouldClose = shouldClose;
    }

    @Override
    protected void init() {
        super.init();

        this.description = add(Label.of("quantum.screen.chunk_load.description"));
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        this.description.setPos(size.width / 2, size.height / 2 - 30);
    }

    public void setDescription(@Nullable String description) {
        this.description.text().setRaw(description);
    }

    public void setDescription(TextObject description) {
        this.description.text().set(description);
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, deltaTime);

        renderer.textCenter(title == null ? TextObject.literal("Chunks loading!") : title, 2, size.width / 2, size.height / 2);

        if (shouldClose.getAsBoolean()) {
            this.close();
        }
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }
}
