package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.libs.commons.v0.util.StringUtils;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.font.Font;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatOverlay extends Overlay {
    private static final Lock messageLock = new ReentrantLock(true);

    public ChatOverlay() {
        super();
    }

    public static void renderChatOverlay(Font font, @NotNull Renderer renderer, boolean showAnyways) {
        int y = QuantumClient.get().getScaledHeight() - 40;
        List<TextObject> messages = ChatScreen.getMessages();
        LongList messageTimestamps = ChatScreen.getMessageTimestamps();
        messageLock.lock();
        try {
            for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
                TextObject text = messages.get(i);
                if (text == null) continue;
                long lineCount = StringUtils.splitIntoLines(text.getText()).size();
                if (lineCount == 0) continue;
                if (lineCount > 1) y -= (int) ((font.lineHeight + 2) * (lineCount - 1));
                long messageTimestamp = messageTimestamps.getLong(i);
                long millisAgo = System.currentTimeMillis() - messageTimestamp;
                if (millisAgo <= 4000 || showAnyways) {
                    if (millisAgo <= 3000 || showAnyways) {
                        renderer.textLeft(text, 10, y, RgbColor.WHITE);
                    } else {
                        int alpha = (int) (255 * (millisAgo - 3000) / 1000) % 1000;
                        renderer.setColor(RgbColor.WHITE.withAlpha(alpha));
                        renderer.setBlitColor(RgbColor.WHITE.withAlpha(alpha));
                        renderer.textLeft(text, 10, y);
                        renderer.setColor(RgbColor.WHITE);
                        renderer.setBlitColor(RgbColor.WHITE);
                    }
                }
                y -= font.lineHeight + 2;
            }
        } catch (Exception e) {
            messageLock.unlock();
            QuantumClient.crash(new CrashLog("Error rendering chat overlay", e));
            throw new Error("Unreachable");
        }
        messageLock.unlock();
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        ChatOverlay.renderChatOverlay(this.font, renderer, false);
    }
}
