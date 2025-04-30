package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Callback;
import dev.ultreon.quantum.client.gui.Position;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.widget.components.CallbackComponent;
import dev.ultreon.quantum.client.gui.widget.components.TextComponent;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static dev.ultreon.quantum.client.QuantumClient.id;
import static dev.ultreon.quantum.client.input.KeyAndMouseInput.*;

public class TextEntry extends Widget {
    private CharPredicate filter = c -> true;

    private int cursorIdx = 0;
    private float cursorX;
    private String value = "";
    private final TextComponent hint;
    private final CallbackComponent<TextEntry> callback;

    private int selectFrom = -1;
    private int selectTo = -1;
    private float yOffset = 0f;

    /**
     * @param width  the width of the text entry.
     * @param height the height of the text entry.
     */
    public TextEntry(int width, int height) {
        super(width, height);

        this.hint = this.register(id("hint"), new TextComponent());
        this.callback = this.register(id("callback"), new CallbackComponent<>(caller -> {
        }));
    }

    public static TextEntry of(String value) {
        TextEntry textEntry = new TextEntry();
        textEntry.value = value;
        return textEntry;
    }

    public static TextEntry of() {
        return new TextEntry();
    }

    @Override
    public TextEntry position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public Widget bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    public TextEntry() {
        this(200, 21);
    }

    public TextEntry(int width) {
        this(width, 21);
    }

    @Override
    public void renderWidget(Renderer renderer, float deltaTime) {
        float yOffsetGoal = isHovered && isEnabled ? 2f : 0f;
        if (this.isFocused && isEnabled) {
            yOffsetGoal = -3f;
        }
        if (yOffset > yOffsetGoal) {
            yOffset -= (yOffset - yOffsetGoal) * Gdx.graphics.getDeltaTime() * 8f;
        } else if (yOffset < yOffsetGoal) {
            yOffset += (yOffsetGoal - yOffset) * Gdx.graphics.getDeltaTime() * 8f;
        }

        renderer.blitColor(RgbColor.WHITE);

        if (!isEnabled) {
            renderer.drawDisabledPlatform(pos.x, pos.y, size.width, size.height, yOffset);
        } else if (isHovered) {
            renderer.drawHighlightPlatform(pos.x, pos.y, size.width, size.height, yOffset);
        } else {
            renderer.drawPlatform(pos.x, pos.y, size.width, size.height, yOffset);
        }

        if (renderer.pushScissors(this.getBounds().shrink(1, 1, 1, 4).move(0, Math.max(0, -yOffset)))) {
            renderText(renderer);
        }
    }

    protected void renderText(Renderer renderer) {
        if (selectFrom != -1 && selectTo != -1) {
            int selectFrom = Math.min(this.selectFrom, this.selectTo);
            int selectTo = Math.max(this.selectFrom, this.selectTo);

            String before = this.value.substring(0, selectFrom);
            String selected = this.value.substring(selectFrom, selectTo);
            String after = this.value.substring(selectTo);

            int beforeWidth = renderer.textWidth(before);
            int selectedWidth = renderer.textWidth(selected);

            renderer.textLeft(before, this.pos.x + 5, this.pos.y - yOffset + this.font.getLineHeight(), RgbColor.WHITE.withAlpha(0x80), true);
            renderer.fill(this.pos.x + 5 + beforeWidth, (int) (this.pos.y - yOffset + this.font.getLineHeight()), selectedWidth, 10, RgbColor.WHITE.withAlpha(0x80));
            renderer.textLeft(selected, this.pos.x + 5 + beforeWidth, this.pos.y - yOffset + this.font.getLineHeight(), RgbColor.RED, true);
            renderer.textLeft(after, this.pos.x + 5 + beforeWidth + selectedWidth, this.pos.y - yOffset + this.font.getLineHeight(), RgbColor.WHITE.withAlpha(0x80), true);
        } else {
            renderer.textLeft(this.value, this.pos.x + 5, this.pos.y - yOffset + this.font.getLineHeight(), true);
        }
        TextObject hintObj = this.hint.get();
        if (this.value.isEmpty() && hintObj != null) {
            renderer.textLeft(hintObj, this.pos.x + 5, this.pos.y - yOffset + this.font.getLineHeight(), RgbColor.WHITE.withAlpha(0x80), true);
        }

        if (this.isFocused) {
            renderer.line(this.pos.x + 3 + this.cursorX, this.pos.y - yOffset + 5, this.pos.x + 3 + this.cursorX, this.pos.y - yOffset + this.size.height - 6, Color.WHITE);
        }

        renderer.popScissors();
    }

    @Override
    public boolean charType(char character) {
        if (!Character.isISOControl(character) && this.filter.test(character)) {
            if (this.selectFrom != -1 && this.selectTo == -1) {
                this.replaceSelection(String.valueOf(character));
                return true;
            }

            var start = this.value.substring(0, this.cursorIdx);
            var end = this.value.substring(this.cursorIdx);
            this.value = start + character + end;
            this.cursorIdx++;
            this.deselect();
            return true;
        }
        return super.charType(character);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.BACKSPACE) {
            if (this.selectFrom != -1 && this.selectTo != -1) {
                deleteSelection();
                return true;
            }
            if (this.cursorIdx > 0) {
                var start = this.value.substring(0, this.cursorIdx - 1);
                var end = this.value.substring(this.cursorIdx);
                this.value = start + end;
                this.cursorIdx--;
                this.deselect();
                return true;
            }
        }
        if (keyCode == Input.Keys.FORWARD_DEL) {
            if (this.selectFrom != -1 && this.selectTo != -1) {
                this.deleteSelection();
                return true;
            }
            if (this.cursorIdx < this.value.length()) {
                var start = this.value.substring(0, this.cursorIdx);
                var end = this.value.substring(this.cursorIdx + 1);
                this.value = start + end;
                this.deselect();
                return true;
            }
        }

        if (keyCode == Input.Keys.LEFT && this.cursorIdx > 0) {
            int nextIdx = this.cursorIdx - 1;
            if (this.isWordMoving()) {
                int index = this.value.lastIndexOf(' ', this.cursorIdx - 1);
                nextIdx = index == -1 ? 0 : index;
            }

            if (!isShiftDown()) {
                this.cursorIdx = nextIdx;
                this.deselect();
                return true;
            }
            if (this.isSelecting()) {
                if (cursorIdx == selectTo) {
                    if (nextIdx < selectFrom) {
                        selectTo = selectFrom;
                        selectFrom = nextIdx;
                    } else if (nextIdx == selectFrom) deselect();
                    else selectTo = nextIdx;
                } else if (cursorIdx == selectFrom) {
                    if (nextIdx > selectTo) selectTo = nextIdx;
                    else if (nextIdx == selectTo) deselect();
                    else selectFrom = nextIdx;
                }
                this.cursorIdx = nextIdx;
                this.revalidateCursor();
                return true;
            }
            selectTo = this.cursorIdx;
            this.cursorIdx = nextIdx;
            selectFrom = this.cursorIdx;
            this.revalidateCursor();
            return true;
        }
        if (keyCode == Input.Keys.RIGHT && this.cursorIdx < this.value.length()) {
            int nextIdx = this.cursorIdx + 1;
            if (this.isWordMoving()) {
                int index = this.value.indexOf(' ', this.cursorIdx + 1);
                nextIdx = index == -1 ? this.value.length() : index;
            }

            if (!isShiftDown()) {
                this.cursorIdx = nextIdx;
                this.deselect();
                return true;
            }
            if (isSelecting()) {
                if (cursorIdx == selectFrom) {
                    if (nextIdx > selectTo) {
                        selectFrom = selectTo;
                        selectTo = nextIdx;
                    } else if (nextIdx == selectTo) deselect();
                    else selectFrom = nextIdx;
                } else if (cursorIdx == selectTo) {
                    if (nextIdx < selectFrom) selectFrom = nextIdx;
                    else if (nextIdx == selectFrom) deselect();
                    else selectTo = nextIdx;
                }
                this.cursorIdx = nextIdx;
                this.revalidateCursor();
                return true;
            }
            selectFrom = this.cursorIdx;
            this.cursorIdx = nextIdx;
            selectTo = this.cursorIdx;
            this.revalidateCursor();
            return true;
        }

        if (keyCode == Input.Keys.HOME) {
            if (!isShiftDown()) {
                this.cursorIdx = 0;
                this.deselect();
                return true;
            }
            selectTo = this.cursorIdx;
            this.cursorIdx = 0;
            selectFrom = this.cursorIdx;
            this.revalidateCursor();
            return true;
        }

        if (keyCode == Input.Keys.END) {
            if (!isShiftDown()) {
                this.cursorIdx = this.value.length();
                this.deselect();
                return true;
            }
            selectFrom = this.cursorIdx;
            this.cursorIdx = this.value.length();
            selectTo = this.cursorIdx;
            this.revalidateCursor();
            return true;
        }

        if (isCtrlDown()) {
            if (keyCode == Input.Keys.V) {
                paste();
                return true;
            } else if (keyCode == Input.Keys.C) {
                copy();
                return true;
            } else if (keyCode == Input.Keys.X) {
                copy();
                deleteSelection();
                return true;
            } else if (keyCode == Input.Keys.A) {
                selectAll();
                return true;
            }
        }

        return super.keyPress(keyCode);
    }

    private boolean isWordMoving() {
        return GamePlatform.get().isMacOSX() ? isAltDown() : isCtrlDown();
    }

    public boolean copy() {
        if (this.selectFrom == -1 || this.selectTo == -1) {
            return false;
        }

        int selectFrom = Math.min(this.selectFrom, this.selectTo);
        int selectTo = Math.max(this.selectFrom, this.selectTo);

        client.clipboard.copy(this.value.substring(selectFrom, selectTo));
        return true;
    }

    private void replaceSelection(String text) {
        if (this.selectFrom == -1 || this.selectTo == -1) {
            this.deselect();
            return;
        }

        if (text == null) text = "";

        int selectFrom = Math.min(this.selectFrom, this.selectTo);
        int selectTo = Math.max(this.selectFrom, this.selectTo);

        var start = this.value.substring(0, selectFrom);
        var end = this.value.substring(selectTo);
        this.value = start + text + end;
        this.cursorIdx++;
        this.deselect();
    }

    private void paste() {
        if (this.selectFrom != -1 && this.selectTo != -1) {
            this.replaceSelection(this.client.clipboard.paste());
            return;
        }

        String pasted = client.clipboard.paste();
        if (pasted == null) {
            this.deselect();
            return;
        }

        var start = this.value.substring(0, this.cursorIdx);
        var end = this.value.substring(this.cursorIdx);
        this.value = start + pasted + end;
        this.cursorIdx += pasted.length();
        this.deselect();
    }

    public void deleteSelection() {
        int selectFrom = Math.min(this.selectFrom, this.selectTo);
        int selectTo = Math.max(this.selectFrom, this.selectTo);

        var start = this.value.substring(0, selectFrom);
        var end = this.value.substring(selectTo);
        this.value = start + end;
        this.cursorIdx = selectFrom;
        this.deselect();
    }

    public void selectAll() {
        this.selectFrom = 0;
        this.selectTo = this.value.length();
        this.revalidateCursor();
    }

    public void deselect() {
        this.selectFrom = -1;
        this.selectTo = -1;
        this.revalidateCursor();
    }

    public boolean isSelecting() {
        return this.selectFrom != -1 && this.selectTo != -1;
    }

    public int getSelectFrom() {
        return Math.min(this.selectFrom, this.selectTo);
    }

    public int getSelectTo() {
        return Math.max(this.selectFrom, this.selectTo);
    }

    public void select(int from, int to) {
        this.selectFrom = from;
        this.selectTo = to;
        this.revalidateCursor();
    }

    public void revalidateCursor() {
        int selFrom = Math.min(this.selectFrom, this.selectTo);
        int selTo = Math.max(this.selectFrom, this.selectTo);

        this.selectFrom = selFrom;
        this.selectTo = selTo;

        if (this.value.isEmpty()) {
            this.cursorX = 0;
            return;
        }

        this.cursorX = this.textWidth(this.value.substring(0, this.cursorIdx)) + 2;

        GamePlatform.get().setTextCursorPos((int) this.cursorX + getX(), getY());

        this.callback.call(this);
    }

    private float textWidth(String substring) {
        return 0;
    }

    @Override
    public void onFocusGained() {
        super.onFocusGained();

        GamePlatform.get().onEnterTextInput();
        this.revalidateCursor();
    }

    @Override
    public void onFocusLost() {
        super.onFocusLost();

        GamePlatform.get().onExitTextInput();
    }

    @Override
    public String getName() {
        return "TextEntry";
    }

    public String getValue() {
        return this.value;
    }

    public TextEntry value(String value) {
        this.value = value;
        return this;
    }

    public TextEntry hint(TextObject text) {
        this.hint.set(text);
        return this;
    }

    public TextEntry filter(CharPredicate filter) {
        this.filter = filter;
        return this;
    }

    public TextEntry callback(Callback<TextEntry> callback) {
        this.callback.set(callback);
        return this;
    }

    public TextComponent hint() {
        return this.hint;
    }

    @ApiStatus.Internal
    public CharPredicate getFilter() {
        return this.filter;
    }

    public int getCursorIdx() {
        return this.cursorIdx;
    }

    public void setCursorIdx(int cursorIdx) {
        this.cursorIdx = cursorIdx;
    }

    public CallbackComponent<TextEntry> callback() {
        return this.callback;
    }

    public TextObject getHint() {
        return this.hint.get();
    }

    public void setValue(String name) {
        this.value = name;
    }
}
