package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.Input;
import dev.ultreon.quantum.client.gui.widget.TextEntry;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.jetbrains.annotations.NotNull;

public class JavascriptDebuggerScreen extends Screen {
    private static TextEntry jsInput;
    private static String output;

    private static final Context CTX = Context.newBuilder("js").allowAllAccess(true).build();

    static {
        CTX.eval("js", "function add(x, y) { return x + y; }");
        CTX.eval("js", "function sub(x, y) { return x - y; }");
        CTX.eval("js", "function mul(x, y) { return x * y; }");
        CTX.eval("js", "function div(x, y) { return x / y; }");
        CTX.eval("js", "function mod(x, y) { return x % y; }");
        CTX.eval("js", "function pow(x, y) { return x ** y; }");
        CTX.eval("js", "function sqrt(x) { return Math.sqrt(x); }");

        CTX.eval("js", "function log(x) { return Math.log(x); }");
        CTX.eval("js", "function log10(x) { return Math.log10(x); }");
        CTX.eval("js", "function log2(x) { return Math.log2(x); }");

        CTX.eval("js", "function sin(x) { return Math.sin(x); }");
        CTX.eval("js", "function cos(x) { return Math.cos(x); }");
        CTX.eval("js", "function tan(x) { return Math.tan(x); }");

        CTX.eval("js", "function asin(x) { return Math.asin(x); }");
        CTX.eval("js", "function acos(x) { return Math.acos(x); }");
        CTX.eval("js", "function atan(x) { return Math.atan(x); }");

        CTX.eval("js", "function asinh(x) { return Math.asinh(x); }");
        CTX.eval("js", "function acosh(x) { return Math.acosh(x); }");
        CTX.eval("js", "function atanh(x) { return Math.atanh(x); }");

        CTX.eval("js", "function exp(x) { return Math.exp(x); }");
        CTX.eval("js", "function ln(x) { return Math.log(x); }");

        CTX.eval("js", "let client = Java.type('dev.ultreon.quantum.client.QuantumClient').get();");
        CTX.eval("js", "let ClientWorld = Java.type('dev.ultreon.quantum.client.world.ClientWorld');");
        CTX.eval("js", "let off = ClientWorld.ATLAS_OFFSET;");
        CTX.eval("js", "let size = ClientWorld.ATLAS_SIZE;");

        CTX.eval("js", "function f(o) { return Java.type('java.lang.Float').parseFloat(o.toString()); }");
        CTX.eval("js", "function d(o) { return Java.type('java.lang.Double').parseDouble(o.toString()); }");
        CTX.eval("js", "function b(o) { return Java.type('java.lang.Byte').parseByte(o.toString()); }");
        CTX.eval("js", "function s(o) { return Java.type('java.lang.Short').parseShort(o.toString()); }");
        CTX.eval("js", "function i(o) { return Java.type('java.lang.Integer').parseInt(o.toString()); }");
        CTX.eval("js", "function l(o) { return Java.type('java.lang.Long').parseLong(o.toString()); }");

        Runtime.getRuntime().addShutdownHook(new Thread(CTX::close));
    }

    public JavascriptDebuggerScreen() {
        super("JS Console");
    }

    @Override
    public void build(GuiBuilder builder) {
        if (jsInput != null) {
            builder.add(jsInput);
            return;
        }

        jsInput = builder.add(
                (TextEntry) TextEntry.of()
                        .hint(TextObject.translation("quantum.screen.javascript_debugger.hint"))
                        .bounds(() -> {
                            if (client.screen != null) {
                                return new Bounds(0, 0, client.screen.getWidth(), 21);
                            } else {
                                return new Bounds(0, 0, 10, 21);
                            }
                        })
        );
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        renderer.textLeft("Output:", 0, 30, ColorCode.GOLD);
        renderer.textLeft(output, 0, 42);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.F5) {
            String value = jsInput.getValue();
            try {
                Value js = CTX.eval("js", value);
                Object hostObject = js.asHostObject();

                this.output = hostObject.toString();
            } catch (Throwable e) {
                this.output = e.toString();
            }
            return true;
        }

        return super.keyPress(keyCode);
    }
}
