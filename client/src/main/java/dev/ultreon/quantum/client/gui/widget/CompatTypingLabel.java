package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.github.tommyettinger.textra.TypingAdapter;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.TypingListener;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.Dialog;
import dev.ultreon.quantum.client.gui.DialogBuilder;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.text.UITranslations;
import dev.ultreon.quantum.text.TextObject;

public class CompatTypingLabel extends TypingLabel implements TypingListener {
    private final Widget qParent;
    private final Vector3 temp = new Vector3();
    private final InputEvent event = new InputEvent();
    private final Matrix4 backupMatrix = new Matrix4();

    public CompatTypingLabel(Widget parent) {
        this.qParent = parent;
        this.setFont(parent.font);
        this.setTextSpeed(0.001f);

        setTypingListener(new TypingAdapter() {
            @Override
            public void event(String event) {
                super.event(event);

                if (event != null) {
                    String[] split = event.split("\\|", 2);
                    if (split.length == 2) {
                        String type = split[0].toLowerCase();
                        String value = split[1];
                        switch (type) {
                            case "url":
                                if (urlClickListener != null) {
                                    urlClickListener.invoke(value);
                                }
                                break;
//                            case "command":
//                                quantum.commandHandler.handleCommand(value);
//                                break;
//                            case "suggest":
//                                quantum.commandHandler.suggestCommand(value);
//                                break;
                            case "copy":
                                QuantumClient.get().clipboard.copy(value);
                                break;
                            default:
                                QuantumClient.LOGGER.warn("Unknown event type: " + type);
                                break;
                        }
                    }
                }
            }
        });

        this.skipToTheEnd();
    }

    @Override
    public void restart(CharSequence newText) {
        super.restart(newText);
        this.skipToTheEnd();
    }

    @Override
    public void setText(String newText, boolean modifyOriginalText, boolean restart) {
        super.setText(" \n" + newText, modifyOriginalText, restart);
    }

    private final UrlClickListener urlClickListener = url -> {
        Screen screen = QuantumClient.get().screen;
        if (screen instanceof Screen) {
            screen.showDialog(new DialogBuilder(screen).message(TextObject.nullToEmpty(url)).title(TextObject.literal("Open link?")).button(UITranslations.CANCEL, Dialog::close).button(UITranslations.PROCEED, () -> Gdx.net.openURI(url)));
        }
    };

    @Override
    public Vector2 screenToLocalCoordinates(Vector2 screenCoords) {
        Vector3 translation = QuantumClient.get().renderer.getTransform().getTranslation(temp);
        return screenCoords.set(screenCoords.x / QuantumClient.get().getGuiScale(), screenCoords.y / QuantumClient.get().getGuiScale())
                .sub(translation.x, translation.y);
    }

    public boolean touchUp(float x, float y, int button, int pointer) {
        InputEvent event1 = event;
        event1.setPointer(pointer);
        event1.setButton(button);
        event1.setStageX(x);
        event1.setStageY(y);
        event1.setType(InputEvent.Type.touchUp);
        return fire(event1);
    }

    public boolean touchDown(float x, float y, int button, int pointer) {
        InputEvent event1 = event;
        event1.setPointer(pointer);
        event1.setButton(button);
        event1.setStageX(x);
        event1.setStageY(y);
        event1.setType(InputEvent.Type.touchDown);
        return fire(event1);
    }

    @Override
    public void event(String event) {
    }

    @Override
    public void end() {
    }

    @Override
    public String replaceVariable(String variable) {
        return null;
    }

    @Override
    public void onChar(long ch) {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.backupMatrix.set(batch.getProjectionMatrix());
        batch.flush();
        batch.getProjectionMatrix().setToOrtho2D(0, 0, qParent.client.getScaledWidth(), qParent.client.getScaledHeight());
        super.draw(batch, parentAlpha);
        batch.flush();
        batch.getProjectionMatrix().set(backupMatrix);
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, qParent.client.getScaledHeight() - getHeight() - y);
    }

    @Override
    public void setPosition(float x, float y, int alignment) {
        super.setPosition(x, qParent.client.getScaledHeight() - getHeight() - y, alignment);
    }

    @Override
    public void setBounds(float x, float y, float width, float height) {
        super.setBounds(x, qParent.client.getScaledHeight() - height - y, width, height);
    }

    @Override
    public float getY() {
        return qParent.client.getScaledHeight() - getHeight() - super.getY();
    }

    @Override
    public void setY(float y) {
        super.setY(qParent.client.getScaledHeight() - getHeight() - y);
    }

    @Override
    public void setY(float y, int alignment) {
        super.setY(qParent.client.getScaledHeight() - getHeight() - y, alignment);
    }

    @Override
    public float getY(int alignment) {
        return qParent.client.getScaledHeight() - getHeight() - super.getY(alignment);
    }

    public interface UrlClickListener {
        void invoke(String url);
    }
}
