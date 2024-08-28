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
        CTX.eval("js", "function add(setX, setY) { return setX + setY; }");
        CTX.eval("js", "function sub(setX, setY) { return setX - setY; }");
        CTX.eval("js", "function mul(setX, setY) { return setX * setY; }");
        CTX.eval("js", "function div(setX, setY) { return setX / setY; }");
        CTX.eval("js", "function mod(setX, setY) { return setX % setY; }");
        CTX.eval("js", "function pow(setX, setY) { return setX ** setY; }");
        CTX.eval("js", "function sqrt(setX) { return Math.sqrt(setX); }");

        CTX.eval("js", "function log(setX) { return Math.log(setX); }");
        CTX.eval("js", "function log10(setX) { return Math.log10(setX); }");
        CTX.eval("js", "function log2(setX) { return Math.log2(setX); }");

        CTX.eval("js", "function sin(setX) { return Math.sin(setX); }");
        CTX.eval("js", "function cos(setX) { return Math.cos(setX); }");
        CTX.eval("js", "function tan(setX) { return Math.tan(setX); }");

        CTX.eval("js", "function asin(setX) { return Math.asin(setX); }");
        CTX.eval("js", "function acos(setX) { return Math.acos(setX); }");
        CTX.eval("js", "function atan(setX) { return Math.atan(setX); }");

        CTX.eval("js", "function asinh(setX) { return Math.asinh(setX); }");
        CTX.eval("js", "function acosh(setX) { return Math.acosh(setX); }");
        CTX.eval("js", "function atanh(setX) { return Math.atanh(setX); }");

        CTX.eval("js", "function exp(setX) { return Math.exp(setX); }");
        CTX.eval("js", "function ln(setX) { return Math.log(setX); }");

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
    public void build(@NotNull GuiBuilder builder) {
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
                if (js.isHostObject()) {
                    Object hostObject = js.asHostObject();
                    this.output = hostObject.toString();
                } else if (js.isNumber()) {
                    this.output = js.asDouble() + "";
                } else if (js.isBoolean()) {
                    this.output = js.asBoolean() + "";
                } else if (js.isString()) {
                    this.output = js.asString();
                } else if (js.isNull()) {
                    this.output = "null";
                } else if (js.isDate()) {
                    this.output = js.asDate().toString();
                } else if (js.isDuration()) {
                    this.output = js.asDuration().toString();
                } else if (js.isException()) {
                    this.output = js.asProxyObject().toString();
                } else if (js.isHostObject()) {
                    this.output = js.asHostObject().toString();
                } else if (js.isProxyObject()) {
                    this.output = js.asProxyObject().toString();
                } else if (js.isIterator()) {
                    this.output = js.asProxyObject().toString();
                } else {
                    this.output = "Invalid JS result";
                }

            } catch (Throwable e) {
                this.output = e.toString();
            }
            return true;
        }

        return super.keyPress(keyCode);
    }
}
