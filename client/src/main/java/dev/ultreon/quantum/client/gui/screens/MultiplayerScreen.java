package dev.ultreon.quantum.client.gui.screens;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.ServerInfo;
import dev.ultreon.quantum.client.gui.Dialog;
import dev.ultreon.quantum.client.gui.DialogBuilder;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.*;
import dev.ultreon.quantum.client.text.Language;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.Nullable;

public class MultiplayerScreen extends Screen {
    public static final Color WHITE_TRANSPARENT = new Color(1, 1, 1, 0.2f);
    private TextEntry entry;
    private TextButton addButton;
    private TextButton joinButton;
    private TextButton backButton;
    private TextButton removeButton;
    private Panel panel;
    private SelectionList<ServerEntry> selectionList = new SelectionList<>();
    private ServerInfo server;
    private Panel listPanel;
    private Panel listButtonPanel;

    public MultiplayerScreen() {
        super(Language.translate("quantum.screen.multiplayer"));
    }

    public MultiplayerScreen(@Nullable Screen back) {
        super(TextObject.translation("quantum.screen.multiplayer"), back);
    }

    @Override
    protected void init() {
        super.init();

        listPanel = add(Panel.create());
        listPanel.setPos(0, 0);
        listPanel.setSize(200, size.height - 30);

        listButtonPanel = add(Panel.create());
        listButtonPanel.setPos(0, size.height - 30);
        listButtonPanel.setSize(200, 30);

        selectionList = add(new SelectionList<>());
        selectionList.setPos(2, 0);
        selectionList.setSize(196, size.height - 30);
        selectionList.drawBackground(false);
        selectionList.selectable(true);
        selectionList.itemRenderer((renderer, value, y, mouseX, mouseY, selected, deltaTime) -> {
            ServerInfo info = value.info;

            renderer.renderFrame(2, y, 196, selectionList.getItemHeight());
            if (selected) renderer.fill(2, y, 196, selectionList.getItemHeight(), WHITE_TRANSPARENT);

            //noinspection IntegerDivisionInFloatingPointContext
            renderer.textCenter("[*]" + info.name(), 100, y + selectionList.getItemHeight() / 2 - font.getLineHeight() / 2, true);
        });
        selectionList.itemHeight(30);
        selectionList.callback(this::selectServer);

        addButton = add(TextButton.of(TextObject.translation("quantum.screen.multiplayer.add"), 190));
        addButton.setPos(5, this.size.height - 26);
        addButton.setType(Button.Type.DARK_EMBED);
        addButton.setCallback(this::addServer);

        client.localData.servers.forEach(info -> selectionList.entry(new ServerEntry(info)));

        panel = add(Panel.create());
        panel.setPos(200 + (this.size.width - 200) / 2 - 55, this.size.height / 2 - 47);
        panel.setSize(110, 92);

        backButton = add(TextButton.of(UITranslations.BACK, 98));
        backButton.setPos(panel.getPos().x + 5, panel.getPos().y + 65);
        backButton.setType(Button.Type.DARK_EMBED);
        backButton.setCallback(this::back);

        joinButton = add(TextButton.of(TextObject.translation("quantum.screen.multiplayer.join"), 98));
        joinButton.setPos(panel.getPos().x + 5, panel.getPos().y + 5);
        joinButton.setType(Button.Type.DARK_EMBED);
        joinButton.setCallback(this::joinServer);
        joinButton.disable();

        removeButton = add(TextButton.of(TextObject.translation("quantum.screen.multiplayer.remove"), 98));
        removeButton.setPos(panel.getPos().x + 5, panel.getPos().y + 35);
        removeButton.setType(Button.Type.DARK_EMBED);
        removeButton.setCallback(this::removeServer);
    }

    private void removeServer(TextButton textButton) {
        if (server == null) return;
        showDialog(new DialogBuilder(this).title(TextObject.translation("quantum.screen.multiplayer.remove_server.title")).message(TextObject.translation("quantum.screen.multiplayer.remove_server", server.name())).button(TextObject.translation("quantum.screen.multiplayer.remove"), () -> removeServer()));
    }

    private void addServer(TextButton textButton) {
        Dialog dialog = new AddServerDialog(this);
        showDialog(dialog);
    }

    private void addServer(ServerInfo serverInfo) {
        selectionList.entry(new ServerEntry(serverInfo));
        client.localData.servers.add(serverInfo);
        client.localData.save();
    }

    private void removeServer() {
        ServerEntry selected = selectionList.getSelected();
        client.localData.servers.remove(selected.info);
        client.localData.save();
        selectionList.removeEntry(selected);
    }

    private void selectServer(ServerEntry serverEntry) {
        this.server = serverEntry.info;
        joinButton.enable();
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        listPanel.setPos(0, 0);
        listPanel.setSize(200, size.height - 30);

        listButtonPanel.setPos(0, size.height - 30);
        listButtonPanel.setSize(200, 30);

        selectionList.setPos(2, 0);
        selectionList.setSize(196, size.height - 30);

        addButton.setPos(5, this.size.height - 26);

        panel.setPos(200 + (this.size.width - 200) / 2 - 55, this.size.height / 2 - 47);
        panel.setSize(110, 92);

        joinButton.setPos(panel.getPos().x + 5, panel.getPos().y + 5);
        removeButton.setPos(panel.getPos().x + 5, panel.getPos().y + 35);
        backButton.setPos(panel.getPos().x + 5, panel.getPos().y + 65);
    }

    private void back(TextButton textButton) {
        this.back();
    }

    private void joinServer(TextButton caller) {
        caller.isEnabled = false;
        MessageScreen messageScreen = new MessageScreen(TextObject.translation("quantum.screen.message.joining_server"));
        this.client.showScreen(messageScreen);

        ServerEntry selected = this.selectionList.getSelected();
        if (selected == null) return;
        String[] split = selected.info.address().split(":", 2);
        if (split.length < 2) {
            return;
        }

        try {
            this.client.serverInfo = selected.info;
            this.client.connectToServer(split[0], Integer.parseInt(split[1]));
        } catch (Exception e) {
            QuantumClient.LOGGER.error("Can't connect to server", e);
        }
    }

    private class AddServerDialog extends Dialog {
        private final TextEntry nameEntry;
        private final TextEntry entry;
        private final TextButton addButton;

        public AddServerDialog(MultiplayerScreen parent) {
            super(parent);

            setSize(200, 110);
            setPos(parent.getWidth() / 2 - size.width / 2, parent.getHeight() / 2 - size.height / 2);
            title = TextObject.translation("quantum.screen.multiplayer.add_server");

            nameEntry = add(TextEntry.of());
            nameEntry.setPos(pos.x + 5, pos.y + 30);
            nameEntry.setSize(190, 20);
            nameEntry.hint().set(TextObject.translation("quantum.screen.multiplayer.server_name"));

            entry = add(TextEntry.of());
            entry.setPos(pos.x + 5, pos.y + 55);
            entry.setSize(190, 20);
            entry.hint().set(TextObject.translation("quantum.screen.multiplayer.server_ip"));
            entry.callback().set(this::validateServerIp);

            addButton = add(TextButton.of(TextObject.translation("quantum.screen.multiplayer.add"), 98));
            addButton.setPos(pos.x + 5, pos.y + 80);
            addButton.setSize(190, 20);
            addButton.setType(Button.Type.DARK_EMBED);
            addButton.setCallback((button) -> {
                MultiplayerScreen multiplayerScreen = MultiplayerScreen.this;
                multiplayerScreen.addServer(new ServerInfo(nameEntry.getValue(), entry.getValue()));
            });

            validateServerIp(entry);
        }

        @Override
        public void revalidate() {
            super.revalidate();

            nameEntry.setPos(pos.x + 5, pos.y + 35);
            entry.setPos(pos.x + 5, pos.y + 60);
            addButton.setPos(pos.x + 5, pos.y + 85);
        }

        private void validateServerIp(TextEntry caller) {
            var text = caller.getValue();
            boolean matches = text.matches("[^:]+:\\d{1,5}");
            if (!matches) {
                this.addButton.isEnabled = false;
                return;
            }
            var split = text.split(":", 2);
            var port = Integer.parseInt(split[1]);

            if (port < 1000 || port > 65535) {
                this.addButton.isEnabled = false;
                return;
            }

            this.addButton.isEnabled = true;
        }
    }
}
