package dev.ultreon.quantum.client.gui.widget.tabs;

import dev.ultreon.quantum.client.gui.widget.UIContainer;

public class Tab extends UIContainer<Tabs> {
    private String title;

    public Tab(String title) {
        super(1, 1);
        this.title = title;
    }

    public void resize(int width, int height) {
        this.size.width = width;
        this.size.height = height;
    }

    public boolean isEmpty() {
        return this.getChildren().isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
