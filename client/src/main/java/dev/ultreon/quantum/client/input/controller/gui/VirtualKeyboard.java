//package dev.ultreon.quantum.client.input.controller.gui;
//
//import com.badlogic.gdx.graphics.Color;
//import dev.ultreon.quantum.client.QuantumClient;
//import dev.ultreon.quantum.client.gui.Renderer;
//import dev.ultreon.quantum.client.gui.widget.Widget;
//import dev.ultreon.quantum.util.RgbColor;
//import org.jetbrains.annotations.ApiStatus;
//import org.jetbrains.annotations.NotNull;
//
//@ApiStatus.Internal
//public class VirtualKeyboard extends Widget {
//    private static final Color BG_COLOR = RgbColor.BLACK.withAlpha(0x80).toGdx();
//    private final TextInputScreen screen;
//
//    public VirtualKeyboard() {
//        super(0, 0);
//        this.screen = new TextInputScreen(this);
//    }
//
//    public void open(VirtualKeyboardEditCallback callback, VirtualKeyboardSubmitCallback submitCallback) {
//        this.screen.setSubmitCallback(submitCallback);
//        this.screen.setEditCallback(callback);
//        this.screen.init(client.getScaledWidth(), client.getScaledHeight());
//    }
//
//    public void close() {
//        this.screen.back();
//        this.screen.setSubmitCallback(() -> {
//        });
//        this.screen.setEditCallback(input -> {
//        });
//        QuantumClient.get().controllerInput.handleVirtualKeyboardClosed(this.screen.getInput());
//    }
//
//    @Override
//    public void render(@NotNull Renderer guiGraphics, float partialTick) {
//        guiGraphics.pushMatrix();
//        guiGraphics.translate(0, 0, Renderer.OVERLAY_ZINDEX);
//        guiGraphics.fill(0, 0, QuantumClient.get().getScaledWidth(), QuantumClient.get().getScaledHeight(), BG_COLOR);
//        this.screen.render(guiGraphics, partialTick);
//        guiGraphics.popMatrix();
//    }
//
//    public TextInputScreen getScreen() {
//        return screen;
//    }
//}
