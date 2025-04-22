package dev.ultreon.quantum.api.ubo;

import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.text.TextObject;

public class UboFormatter {
    public static TextObject format(DataType<?> message) {
        return message.accept(new UboFormatterVisitor(0));
    }
    public static TextObject format(DataType<?> message, int ident) {
        return message.accept(new UboFormatterVisitor(ident));
    }
}
