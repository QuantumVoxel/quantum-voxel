package dev.ultreon.quantum.teavm;

import com.badlogic.gdx.Gdx;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSError;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public class CrashOverlay {
    public static void createOverlay(String message) {
        HTMLDocument document = Window.current().getDocument();

        // Create the overlay div
        HTMLElement overlay = document.createElement("div");
        overlay.getStyle().setProperty("position", "fixed");
        overlay.getStyle().setProperty("top", "0");
        overlay.getStyle().setProperty("left", "0");
        overlay.getStyle().setProperty("width", "100%");
        overlay.getStyle().setProperty("height", "100%");
        overlay.getStyle().setProperty("z-index", "9999");
        overlay.getStyle().setProperty("background-color", "#fff3");
        overlay.getStyle().setProperty("backdrop-blur", "24px");
        overlay.getStyle().setProperty("display", "flex");
        overlay.getStyle().setProperty("justify-content", "center");
        overlay.getStyle().setProperty("align-items", "center");
        overlay.getStyle().setProperty("color", "white");
        overlay.getStyle().setProperty("font-size", "12px");
        overlay.getStyle().setProperty("font-family", "sans-serif");
        overlay.getStyle().setProperty("padding", "20px");

        overlay.setInnerHTML("<div style=\"padding: 20px; border: 3px solid #f46f; border-radius: 14px; max-width: calc(100% - 40px); background-color: #222f; overflow-y: auto; box-shadow: 0 16px 32px #0004;\"><h1 style=\"margin-top: 0; text-align: center; width: 100%; border-bottom: 1px solid #fff3; padding-bottom: 10px;\">Game Crashed</h1><pre>" + escapeHTML(message).replace("\r\n", "<br>")
                .replace("\n", "<br>")
                .replace("\r", "<br>")
                .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
                .replace(" ", "&nbsp;") + "</pre></div>");

        document.getBody().appendChild(overlay);
    }

    @JSBody(params = { "html" }, script = "return html.replace(/[&<>\"']/g, function (c) {" +
                                          "return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '\"': '&quot;', \"'\": '&#39;'}[c]; });")
    private static native String escapeHTML(String html);
}
