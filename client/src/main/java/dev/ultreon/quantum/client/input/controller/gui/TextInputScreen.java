package dev.ultreon.quantum.client.input.controller.gui;

import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.screens.ChatScreen;
import dev.ultreon.quantum.client.input.controller.keyboard.KeyboardLayout;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TextInputScreen extends Screen {
    private final KeyboardLayout layout;
    private final VirtualKeyboard virtualKeyboard;
    private String input;
    private boolean shift;
    private boolean caps;
    private VirtualKeyboardSubmitCallback submitCallback = () -> {};
    private VirtualKeyboardEditCallback editCallback = s -> {};
    private final List<Keycap> buttons = new ArrayList<>();

    public TextInputScreen(VirtualKeyboard virtualKeyboard) {
        super(TextObject.literal("Text Input"));
        this.virtualKeyboard = virtualKeyboard;

        this.layout = client.controllerInput.getLayout();
    }

    public void setSubmitCallback(VirtualKeyboardSubmitCallback callback) {
        this.submitCallback = callback;
    }

    public void setEditCallback(VirtualKeyboardEditCallback callback) {
        this.editCallback = callback;
    }

    @Override
    public void resized(int width, int height) {
        Screen screen = this.parentScreen;
        if (screen != null) {
            screen.resized(width, height);
        }

        super.resized(width, height);
    }

    @Override
    protected void init() {
        this.setInput(client.controllerInput.getVirtualKeyboardValue());

        for (Keycap button : this.buttons) {
            this.remove(button);
        }

        this.buttons.clear();

        char[][] layoutLayout = layout.getLayout(shift || caps);
        for (int rowIdx = 0, layoutLayoutLength = layoutLayout.length; rowIdx < layoutLayoutLength; rowIdx++) {
            char[] row = layoutLayout[rowIdx];

            int keyboardWidth = row.length * 16;
            if (rowIdx == 0) keyboardWidth += 16;
            if (rowIdx == 1) keyboardWidth += 7;
            if (rowIdx == 2) keyboardWidth += 27;
            if (rowIdx == 3) keyboardWidth += 33;
            if (rowIdx == 4) keyboardWidth += 41;

            int x = this.size.width / 2 - keyboardWidth / 2;
            for (char c : row) {
                KeyMappingIcon icon = KeyMappingIcon.byChar(c);
                if (icon == null) continue;

                this.addButton(c, x, rowIdx, icon);

                x += icon.width;
            }
        }

        super.init();
    }

    @Override
    public void renderBackground(@NotNull Renderer gfx) {

    }

    private void addButton(char c, int x, int rowIdx, KeyMappingIcon icon) {
        Keycap imageButton = this.add(new Keycap(icon, x, rowIdx * 16 + size.height - 85 - getYOffset()).setCallback(button -> {
            if (c >= 0x20) {
                setInput(getInput() + c);
                return;

            }
            switch (c) {
                case '\n', '\r' -> this.submit();
                case '\b' -> this.backspace();
                case '\t' -> setInput(getInput() + "    ");
                case '\0', '\1', '\3', '\4', '\5', '\6', '\7' -> {
                    // TODO: Add support for other controller input characters
                }
            }
        }));

        this.buttons.add(imageButton);
    }

    private int getYOffset() {
        if (this.client != null) {
            return this.client.screen instanceof ChatScreen ? 32 : 0;
        }

        return 0;
    }

    private void submit() {
        virtualKeyboard.close();
        submitCallback.onSubmit();
    }

    private void backspace() {
        if (!getInput().isEmpty()) {
            setInput(getInput().substring(0, getInput().length() - 1));
        }
    }

    @Override
    public void onClosed() {
        this.virtualKeyboard.close();

        this.submitCallback = () -> {};
    }

    public String getInput() {
        return input;
    }

    private void setInput(String input) {
        this.input = input;
        this.editCallback.onInput(input);
    }
}
