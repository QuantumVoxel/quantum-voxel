package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.github.tommyettinger.textra.Layout;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.network.packets.c2s.C2SRequestTabComplete;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.Input.Keys.*;

public class ChatTextEntry extends TextEntry {
    private final ChatScreen screen;
    private final TabCompletePopup popup = new TabCompletePopup(0, 0);
    private final Color backgroundColor = new Color(0, 0, 0, 1f);
    private int completeX = -1;

    public ChatTextEntry(ChatScreen screen) {
        this.screen = screen;
        this.screen.focused = this;
        this.isFocused = true;
    }

    @Override
    public boolean keyPress(int keyCode) {
        switch (keyCode) {
            case ENTER:
                if (Gdx.input.isKeyPressed(CONTROL_LEFT) && this.popup.visible && this.popup.values.length > 0) {
                    this.complete("");
                    this.popup.visible = false;
                } else {
                    this.screen.send();
                }
                return true;
            case TAB:
                if (this.popup.visible && this.popup.values.length > 0) {
                    this.complete("");
                } else {
                    this.client.connection.send(new C2SRequestTabComplete(this.getValue()));
                }
                return true;
            case UP:
                if (this.popup.visible && this.popup.values.length > 0) {
                    this.popup.up();
                }
                break;
            case DOWN:
                if (this.popup.visible && this.popup.values.length > 0) {
                    this.popup.down();
                }
                break;
            case ESCAPE:
                if (this.popup.visible) {
                    this.popup.visible = false;
                    return true;
                }
                break;
        }

        boolean b = super.keyPress(keyCode);

        if (this.getCursorIdx() < this.completeX) {
            this.popup.visible = false;
            this.popup.setValues(new String[0]);
        } else {
            this.completeX = this.getValue().lastIndexOf(' ') + 1;
        }
        return b;
    }

    private void complete(String s) {
        String value = this.revalidateCompleteX();
        if (value.startsWith("/")) {
            this.setValue("/" + value.substring(1, this.completeX) + this.popup.get() + s);
        } else {
            this.completeX = this.getCursorIdx();
            this.setValue(value.substring(0, this.completeX) + this.popup.get() + s);
        }
        this.popup.visible = false;
        this.setCursorIdx(this.getValue().length());
        this.revalidateCursor();
    }

    @Override
    public boolean charType(char character) {
        if (character == ' ' && Gdx.input.isKeyPressed(SHIFT_LEFT) && this.popup.visible && this.popup.values.length > 0) {
            this.complete(" ");
            this.popup.visible = false;
            return true;
        }

        boolean b = super.charType(character);
        if (b) {
            String value = this.revalidateCompleteX();
            this.client.connection.send(new C2SRequestTabComplete(value));
        }
        return b;
    }

    @NotNull
    private String revalidateCompleteX() {
        String value = this.getValue();
        if (value.startsWith("/")) {
            if (value.contains(" ")) {
                this.completeX = value.lastIndexOf(' ') + 1;
            } else {
                this.completeX = 1;
            }
        } else {
            this.completeX = value.length();
        }
        return value;
    }

    @Override
    public void onFocusLost() {
        this.root.focused = this;
    }

    public ChatScreen getScreen() {
        return this.screen;
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, this.backgroundColor);

        if (renderer.pushScissors(this.getBounds().shrink(1, 1, 1, 4))) {
            renderText(renderer);
        }

        this.popup.y = this.getY();
        this.popup.render(renderer, deltaTime);
    }

    public void onTabComplete(String[] options) {
        String s = this.getValue().replaceAll(" .*^", "");
        Layout layout = new Layout();
        layout.setFont(this.font);
        layout.clear();
        this.font.markup(this.getValue().substring(0, s.length()), layout);
        this.completeX = s.length();
        this.popup.x = (int) layout.getWidth();
        this.popup.setValues(options);
        this.popup.visible = true;
    }
}
