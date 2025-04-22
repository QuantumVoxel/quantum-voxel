package dev.ultreon.quantum.client.gui.overlay;

import dev.ultreon.libs.commons.v0.util.StringUtils;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatOverlay extends Overlay {
    public ChatOverlay() {
        super();
    }

    public static void renderChatOverlay(GameFont font, @NotNull Renderer renderer, boolean showAnyways) {
        int y = QuantumClient.get().getScaledHeight() - 40;
        List<TextObject> messages = ChatScreen.getMessages();
        LongList messageTimestamps = ChatScreen.getMessageTimestamps();
        try {
            for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
                TextObject text = messages.get(i);
                if (text == null) continue;
                long lineCount = StringUtils.splitIntoLines(text.getText()).size();
                if (lineCount == 0) continue;
                if (lineCount > 1) y -= (int) ((font.getLineHeight() + 2) * (lineCount - 1));
                long messageTimestamp = messageTimestamps.getLong(i);
                long millisAgo = System.currentTimeMillis() - messageTimestamp;
                if (millisAgo <= 4000 || showAnyways) {
                    if (millisAgo <= 3000 || showAnyways) {
                        renderer.textLeft("[lighter red][[[gold]" + DateTimeFormatter.ISO_TIME.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(messageTimestamp / 1000), ZoneId.systemDefault())) + "[lighter red]] [ ]" + text, 10, y, RgbColor.WHITE);
                    } else {
                        int alpha = (int) (255 * (millisAgo - 3000) / 1000) % 1000;
                        renderer.setColor(RgbColor.WHITE.withAlpha(alpha));
                        renderer.setBlitColor(RgbColor.WHITE.withAlpha(alpha));
                        renderer.textLeft("[lighter red][[[gold]" + DateTimeFormatter.ISO_TIME.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(messageTimestamp / 1000), ZoneId.systemDefault())) + "[lighter red]] [ ]" + text, 10, y);
                        renderer.setColor(RgbColor.WHITE);
                        renderer.setBlitColor(RgbColor.WHITE);
                    }
                }
                y -= font.getLineHeight() + 2;
            }
        } catch (Exception e) {
            QuantumClient.crash(new CrashLog("Error rendering chat overlay", e));
            throw new Error("Unreachable");
        }
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        ChatOverlay.renderChatOverlay(this.font, renderer, false);
    }
}
