package dev.ultreon.quantum.client.gui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.github.tommyettinger.textra.TypingAdapter;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.TypingListener;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Screen;

public class CompatTypingLabel extends TypingLabel implements TypingListener {
    private final Widget qParent;
    private final Vector3 temp = new Vector3();
    private final InputEvent event = new InputEvent();

    public CompatTypingLabel(Widget parent) {
        this.qParent = parent;

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
    }

    private UrlClickListener urlClickListener = url -> {
//        Screen screen = QuantumClient.get().screen;
//        if (screen instanceof Screen) {
//            QuantumClient.get().showScreen(new ConfirmationScreen((Screen) screen, "[lighter red]Do you want to open this link?", url, () -> {
//                Gdx.net.openURI(url);
//            }));
//        }

        Gdx.net.openURI(url);
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

    public interface UrlClickListener {
        void invoke(String url);
    }
}
