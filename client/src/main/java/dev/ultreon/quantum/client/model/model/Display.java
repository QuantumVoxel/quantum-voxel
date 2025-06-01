package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.utils.JsonValue;

public final class Display {
    public String renderPass;

    public Display(String renderPass) {
        this.renderPass = renderPass;
    }

    public static Display read(JsonValue display) {
        JsonValue renderPassJson = display.get("renderPass");
        String renderPass = renderPassJson != null ? renderPassJson.asString() : "opaque";
        return new Display(renderPass);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "Display[]";
    }


}
