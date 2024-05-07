package dev.ultreon.quantum.api.ubo;

import com.ultreon.data.types.IType;
import dev.ultreon.quantum.text.TextObject;

public class UboFormatter {
    public static TextObject format(IType<?> message) {
        return message.accept(new UboFormatterVisitor(0));
    }
    public static TextObject format(IType<?> message, int ident) {
        return message.accept(new UboFormatterVisitor(ident));
    }
}
