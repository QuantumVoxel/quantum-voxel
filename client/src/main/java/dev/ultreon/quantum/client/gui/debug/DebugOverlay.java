package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

public class DebugOverlay {
    private static final int OFFSET = 10;
    private static final Color COLOR = new Color(0, 0, 0, 0.5F);
    final QuantumClient client;
    private int leftY;
    private int rightY;
    private int page = 1;
    private int scale;

    public DebugOverlay(QuantumClient client) {
        this.client = client;
    }

    public void render(Renderer renderer, int scale) {
        this.leftY = DebugOverlay.OFFSET;
        this.rightY = DebugOverlay.OFFSET;

        this.scale = scale;

        left(renderer, "Page", this.page);
        left();

        this.renderPage(renderer);
    }

    private void renderPage(Renderer renderer) {
        DebugPage page = this.getPage();
        if (page == null) return;
        page.render(new RenderDebugPageContext(this, renderer));
    }

    public void nextPage() {
        var page = this.page + 1;
        if (!this.client.isShowDebugHud()) {
            page = 0;
            this.client.setShowDebugHud(true);
        }
        if (page >= ClientRegistries.DEBUG_PAGE.size()) {
            this.client.setShowDebugHud(false);
        }

        this.page = page;
    }

    public void prevPage() {
        var page = this.page - 1;
        if (!this.client.isShowDebugHud()) {
            page = ClientRegistries.DEBUG_PAGE.size() - 1;
            this.client.setShowDebugHud(true);
        }
        if (page < 0) {
            this.client.setShowDebugHud(false);
        }

        this.page = page;
    }

    private DebugPage getPage() {
        if (ClientRegistries.DEBUG_PAGE.isEmpty()) {
            return DebugPage.EMPTY;
        }
        return ClientRegistries.DEBUG_PAGE.byRawId(this.page);
    }

    public DebugOverlay left(Renderer renderer, String name, Object value) {
        String textObject = escape(name) + ": [light grey]" + escape(String.valueOf(value));
        int width = renderer.textWidth(textObject);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, width + 5, 11, COLOR);
        renderer.textLeft(textObject, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    public DebugOverlay left(Renderer renderer, String text) {
        String textObject = "[*][_][gold]" + escape(text);
        int width = renderer.textWidth(textObject);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, width + 5, 11, COLOR);
        renderer.textLeft(textObject, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    public DebugOverlay left() {
        this.leftY += 11;
        return this;
    }

    public DebugOverlay right(Renderer renderer, String name, Object value) {
        String textObject = escape(name) + ": [light grey]" + escape(String.valueOf(value));
        int width = renderer.textWidth(textObject);
        int screenWidth = this.client.getWidth() / scale;
        renderer.fill(screenWidth - DebugOverlay.OFFSET - 3 - width, this.rightY - 1, width + 5, 11, COLOR);
        renderer.textRight(textObject, screenWidth - DebugOverlay.OFFSET, this.rightY);
        this.rightY += 11;
        return this;
    }

    public DebugOverlay right(Renderer renderer, String text) {
        String textObject = "[*][_][gold]" + escape(text);
        int width = renderer.textWidth(textObject);
        int screenWidth = this.client.getWidth() / 2;
        renderer.fill(DebugOverlay.OFFSET - 2, this.rightY - 1, width + 5, 11, COLOR);
        renderer.textRight(textObject, screenWidth - DebugOverlay.OFFSET, this.rightY);
        this.rightY += 11;
        return this;
    }

    public static @NotNull String escape(String text) {
        return text.replace("[", "[[").replace("{", "{{");
    }

    public DebugOverlay right() {
        this.rightY += 11;
        return this;
    }

    public DebugOverlay entryLine(Renderer renderer, int idx, String name, long nanos) {
        String lText = "[*][[" + idx + "] [white]" + name;
        String rText = nanos < 10000.0
                ? "[light grey] < 0.01 [#a0a0a0]ms"
                : String.format("[light grey]%.2f [#a0a0a0]ms", nanos / 1000000.0);
        int lWidth = renderer.textWidth(lText);
        int rWidth = renderer.textWidth(rText);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, COLOR);
        renderer.textLeft(lText, DebugOverlay.OFFSET, this.leftY);
        renderer.textRight(rText, DebugOverlay.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
        return this;
    }

    DebugOverlay entryLine(Renderer renderer, int idx, String name) {
        String text = "[gold][[" + idx + "] [white]" + name;
        int width = renderer.textWidth(text);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, COLOR);
        renderer.textLeft(text, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    DebugOverlay entryLine(Renderer renderer, TextObject text) {
        int width = renderer.textWidth(text);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, COLOR);
        renderer.textLeft(text, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @Deprecated
    DebugOverlay entryLine(Renderer renderer, String text) {
        int width = renderer.textWidth(text);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, COLOR);
        renderer.textLeft(text, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    public DebugOverlay entryLine(Renderer renderer, String name, String value) {
        String lText = "[white]" + name + "[ ]";
        String rText = "[light grey]" + value + "[ ]";
        int lWidth = renderer.textWidth(lText);
        int rWidth = renderer.textWidth(rText);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, COLOR);
        renderer.textLeft(lText, DebugOverlay.OFFSET, this.leftY);
        renderer.textRight(rText, DebugOverlay.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
        return this;
    }

    public DebugOverlay entryLine(Renderer renderer) {
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, 304, 11, COLOR);
        this.leftY += 11;
        return this;
    }

    public void updateProfiler() {
        if (this.getPage() instanceof ProfilerDebugPage) {
            ProfilerDebugPage profilerPage = (ProfilerDebugPage) this.getPage();
            var profiler = this.client.profiler;
            profilerPage.profile = profiler.collect();
        }
    }

    public void update() {
        for (DebugPage page : ClientRegistries.DEBUG_PAGE.values()) {
            page.update(page == this.getPage());
        }
    }
}
