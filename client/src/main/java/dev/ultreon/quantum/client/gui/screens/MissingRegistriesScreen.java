package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.Label;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashSet;
import java.util.Set;

public class MissingRegistriesScreen extends Screen {
    private final Set<NamespaceID> clientSet = new HashSet<>();
    private final Set<NamespaceID> serverSet = new HashSet<>();
    private Label descriptionLabel;
    private SelectionList<NamespaceID> selectionList;

    public MissingRegistriesScreen(Set<NamespaceID> client) {
        super(TextObject.translation("quantum.screen.missing_registries.title"));

        clientSet.addAll(client);
    }

    public MissingRegistriesScreen(Set<NamespaceID> client, Set<NamespaceID> server) {
        super(TextObject.translation("quantum.screen.missing_registries.title"));

        clientSet.addAll(client);
        serverSet.addAll(server);
    }

    @Override
    protected void init() {
        super.init();

        descriptionLabel = add(Label.of("quantum.screen.missing_registries.description"));

        selectionList = add(new SelectionList<>());
        selectionList.setPos(this.size.width / 2, this.size.height / 2);
        selectionList.setSize(200, 100);
        selectionList.withDrawBackground(false);
        selectionList.withSelectable(true);
        selectionList.withItemRenderer(this::renderItem);
        selectionList.withItemHeight(20);
        selectionList.withCallback(value -> client.clipboard.copy(value.toString()));
        selectionList.addEntries(clientSet);

        add(TextButton.of("Back", 50))
                .withCallback(button -> client.showScreen(new TitleScreen()))
                .withBounding(70, 10, 50, 20);
    }

    @Override
    public void resized(int width, int height) {
        super.resized(width, height);

        descriptionLabel.setPos(size.width / 2, size.height / 3);
    }

    private void renderItem(Renderer renderer,
                            NamespaceID value,
                            int y,
                            boolean selected,
                            float deltaTime) {
        renderer.textCenter(value.toString(), this.selectionList.getX() + this.selectionList.getWidth() / 2, y);
    }
}
