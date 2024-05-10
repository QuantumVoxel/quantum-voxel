package dev.ultreon.quantum.client.gui.debug;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;

public class DebugOverlay {
    private static final int OFFSET = 10;
    final QuantumClient client;
    private int leftY;
    private int rightY;
    private int page;

    public DebugOverlay(QuantumClient client) {
        this.client = client;
    }

    public void render(Renderer renderer) {
        this.leftY = DebugOverlay.OFFSET;
        this.rightY = DebugOverlay.OFFSET;

        if (!this.client.isShowDebugHud() || this.client.world == null) return;

        this.renderPage(renderer);
    }

    private void renderPage(Renderer renderer) {
        DebugPage page = this.getPage();
        page.render(new DebugPageContext() {
            @Override
            public DebugPageContext left() {
                DebugOverlay.this.left();
                return this;
            }

            @Override
            public DebugPageContext left(String text) {
                DebugOverlay.this.left(renderer, text);
                return this;
            }

            @Override
            public DebugPageContext left(String key, Object value) {
                DebugOverlay.this.left(renderer, key, value);
                return this;
            }

            @Override
            public DebugPageContext right() {
                DebugOverlay.this.right();
                return this;
            }

            @Override
            public DebugPageContext right(String text) {
                DebugOverlay.this.right(renderer, text);
                return this;
            }

            @Override
            public DebugPageContext right(String key, Object value) {
                DebugOverlay.this.right(renderer, key, value);
                return this;
            }

            @Override
            public DebugPageContext entryLine(int idx, String name, long nanos) {
                DebugOverlay.this.entryLine(renderer, idx, name, nanos);
                return this;
            }

            @Override
            public DebugPageContext entryLine(String name, String value) {
                DebugOverlay.this.entryLine(renderer, name, value);
                return this;
            }

            @Override
            public DebugPageContext entryLine(int idx, String name) {
                DebugOverlay.this.entryLine(renderer, idx, name);
                return this;
            }

            @Override
            public DebugPageContext entryLine(TextObject text) {
                DebugOverlay.this.entryLine(renderer, text);
                return this;
            }

            @Override
            public DebugPageContext entryLine() {
                DebugOverlay.this.entryLine(renderer);
                return this;
            }

            @Override
            public QuantumClient client() {
                return DebugOverlay.this.client;
            }
        });
    }

    public void nextPage() {
        var page = this.page + 1;
        if (!this.client.isShowDebugHud()) {
            page = 0;
            this.client.setShowDebugHud(true);
        }
        if (page >= ClientRegistries.DEBUG_PAGE.values().size()) {
            this.client.setShowDebugHud(false);
        }

        this.page = page;
    }

    public void prevPage() {
        var page = this.page - 1;
        if (!this.client.isShowDebugHud()) {
            page = ClientRegistries.DEBUG_PAGE.values().size() - 1;
            this.client.setShowDebugHud(true);
        }
        if (page < 0) {
            this.client.setShowDebugHud(false);
        }

        this.page = page;
    }

    private DebugPage getPage() {
        if (ClientRegistries.DEBUG_PAGE.entries().isEmpty()) {
            return DebugPage.EMPTY;
        }
        return ClientRegistries.DEBUG_PAGE.byId(this.page);
    }

    @CanIgnoreReturnValue
    public DebugOverlay left(Renderer renderer, String name, Object value) {
        MutableText textObject = TextObject.literal(name).append(": ").append(TextObject.literal(String.valueOf(value)).setColor(RgbColor.LIGHT_GRAY));
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, width + 5, 11, RgbColor.BLACK.withAlpha(128));
        renderer.textLeft(textObject, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay left(Renderer renderer, String text) {
        MutableText textObject = TextObject.literal(text).setBold(true).setUnderlined(true).setColor(RgbColor.GOLD);
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, width + 5, 11, RgbColor.BLACK.withAlpha(128));
        renderer.textLeft(textObject, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay left() {
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay right(Renderer renderer, String name, Object value) {
        MutableText textObject = TextObject.literal(name).append(": ").append(TextObject.literal(String.valueOf(value)).setColor(RgbColor.LIGHT_GRAY));
        int width = renderer.getFont().width(textObject);
        int screenWidth = this.client.getScaledWidth();
        renderer.fill(screenWidth - DebugOverlay.OFFSET - 3 - width, this.rightY - 1, width + 5, 11, RgbColor.BLACK.withAlpha(128));
        renderer.textRight(textObject, screenWidth - DebugOverlay.OFFSET, this.rightY);
        this.rightY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay right(Renderer renderer, String text) {
        MutableText textObject = TextObject.literal(text).setBold(true).setUnderlined(true).setColor(RgbColor.GOLD);
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugOverlay.OFFSET - 2, this.rightY - 1, width + 5, 11, RgbColor.BLACK.withAlpha(128));
        renderer.textRight(textObject, DebugOverlay.OFFSET, this.rightY);
        this.rightY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay right() {
        this.rightY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay entryLine(Renderer renderer, int idx, String name, long nanos) {
        MutableText lText = TextObject.literal("[" + idx + "] ").setColor(RgbColor.GOLD).append(TextObject.literal(name).setColor(RgbColor.WHITE));
        MutableText rText;
        if (nanos < 10000.0)
            rText = TextObject.literal("< 0.01").setColor(RgbColor.LIGHT_GRAY)
                    .append(TextObject.literal(" ms").setColor(RgbColor.rgb(0xa0a0a0)));
        else
            rText = TextObject.literal("%.2f".formatted(nanos / 1000000.0)).setColor(RgbColor.LIGHT_GRAY)
                    .append(TextObject.literal(" ms").setColor(RgbColor.rgb(0xa0a0a0)));
        int lWidth = renderer.getFont().width(lText);
        int rWidth = renderer.getFont().width(rText);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, RgbColor.BLACK.withAlpha(128));
        renderer.textLeft(lText, DebugOverlay.OFFSET, this.leftY);
        renderer.textRight(rText, DebugOverlay.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    DebugOverlay entryLine(Renderer renderer, int idx, String name) {
        MutableText text = TextObject.literal("[" + idx + "] ").setColor(RgbColor.GOLD).append(TextObject.literal(name).setColor(RgbColor.WHITE));
        int width = renderer.getFont().width(text);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, RgbColor.BLACK.withAlpha(128));
        renderer.textLeft(text, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    DebugOverlay entryLine(Renderer renderer, TextObject text) {
        int width = renderer.getFont().width(text);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, RgbColor.BLACK.withAlpha(128));
        renderer.textLeft(text, DebugOverlay.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay entryLine(Renderer renderer, String name, String value) {
        MutableText lText = TextObject.literal(name).setColor(RgbColor.WHITE);
        MutableText rText = TextObject.literal(value).setColor(RgbColor.LIGHT_GRAY);
        int lWidth = renderer.getFont().width(lText);
        int rWidth = renderer.getFont().width(rText);
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, RgbColor.BLACK.withAlpha(128));
        renderer.textLeft(lText, DebugOverlay.OFFSET, this.leftY);
        renderer.textRight(rText, DebugOverlay.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugOverlay entryLine(Renderer renderer) {
        renderer.fill(DebugOverlay.OFFSET - 2, this.leftY - 1, 304, 11, RgbColor.BLACK.withAlpha(128));
        this.leftY += 11;
        return this;
    }

    public void updateProfiler() {
        if (this.getPage() instanceof ProfilerDebugPage profilerPage) {
            var profiler = this.client.profiler;
            profilerPage.profile = profiler.collect();
        }
    }
}
