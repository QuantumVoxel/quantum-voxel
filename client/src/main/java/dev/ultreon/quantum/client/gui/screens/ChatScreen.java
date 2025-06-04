package dev.ultreon.quantum.client.gui.screens;

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
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.jetbrains.annotations.NotNull;

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

        QuantumClient.LOGGER.info("Received message: {}", message.getText());

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
        this.entry = (ChatTextEntry) builder.add(new ChatTextEntry(this).withBounding(() -> new Bounds(-1, this.getHeight() - 21, this.getWidth() + 2, 21)));
        if (this.input != null) {
            this.entry.value(this.input);
            this.entry.setCursorIdx(this.input.length());
            this.entry.revalidateCursor();
        }
    }

    public ChatTextEntry getEntry() {
        return this.entry;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, float deltaTime) {
        ChatOverlay.renderChatOverlay(this.font, renderer, true);

        super.renderWidget(renderer, deltaTime);
    }

    @Override
    protected void renderTransparentBackground(Renderer renderer) {
        /*
         Do not render the transparent background, just pass through the game directly.
         If we remove the blur, people can see what they're chatting about.
         No need to get some glasses :P
        */
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

}
