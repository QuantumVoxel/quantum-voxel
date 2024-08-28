package dev.ultreon.quantum.client.gui.screens;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.overlay.ChatOverlay;
import dev.ultreon.quantum.client.gui.widget.ChatTextEntry;
import dev.ultreon.quantum.network.packets.c2s.C2SChatPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SCommandPacket;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatScreen extends Screen {
    private final String input;
    private ChatTextEntry entry;
    private static final List<TextObject> MESSAGES = new CopyOnWriteArrayList<>();
    private static final LongList MESSAGE_TIMESTAMPS = LongLists.synchronize(new LongArrayList());

    public ChatScreen(String input) {
        super("");
        this.input = input;
    }

    public ChatScreen() {
        this("");
    }

    public static void addMessage(TextObject message) {
        ChatScreen.MESSAGES.add(0, message);
        ChatScreen.MESSAGE_TIMESTAMPS.add(0, System.currentTimeMillis());

        QuantumClient.LOGGER.info("Received message: " + message.getText());

        if (ChatScreen.MESSAGES.size() > 100) {
            ChatScreen.MESSAGES.remove(ChatScreen.getMessages().size() - 1);
            ChatScreen.MESSAGE_TIMESTAMPS.removeLong(ChatScreen.getMessages().size());
        }
    }

    public static List<TextObject> getMessages() {
        return Collections.unmodifiableList(ChatScreen.MESSAGES);
    }

    public static LongList getMessageTimestamps() {
        return LongLists.unmodifiable(ChatScreen.MESSAGE_TIMESTAMPS);
    }

    @Override
    public void build(@NotNull GuiBuilder builder) {
        this.entry = (ChatTextEntry) builder.add(new ChatTextEntry(this).bounds(() -> new Bounds(-1, this.getHeight() - 21, this.getWidth() + 2, 21)));
        if (this.input != null) {
            this.entry.value(this.input);
            this.entry.setCursorIdx(this.input.length());
            this.entry.revalidateCursor();
        }
    }

    public ChatTextEntry getEntry() {
        return (ChatTextEntry) this.entry;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        ChatOverlay.renderChatOverlay(this.font, renderer, true);

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }

    public void send() {
        var input = this.entry.getValue();
        if (input.startsWith("/")) {
            if (this.client.connection == null) return;
            this.client.connection.send(new C2SCommandPacket(input.substring(1)));
            this.close();
            return;
        }

        this.client.connection.send(new C2SChatPacket(input));
        this.close();
    }

    public void onTabComplete(String[] options) {
        this.entry.onTabComplete(options);
    }

    private static class AWTColorTypeAdapter extends TypeAdapter<java.awt.Color> {
        @Override
        public void write(JsonWriter out, java.awt.Color value) throws IOException {
            out.value(value.getRGB());
        }

        @Override
        public java.awt.Color read(JsonReader in) throws IOException {
            return new java.awt.Color(in.nextInt(), true);
        }
    }

    private static class QuantumColorTypeAdapter extends TypeAdapter<RgbColor> {
        @Override
        public void write(JsonWriter out, RgbColor value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public RgbColor read(JsonReader in) throws IOException {
            return RgbColor.hex(in.nextString());
        }
    }
}
