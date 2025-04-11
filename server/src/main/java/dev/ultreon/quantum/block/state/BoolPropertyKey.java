package dev.ultreon.quantum.block.state;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class BoolPropertyKey extends StatePropertyKey<Boolean> {
    public BoolPropertyKey(String name) {
        super(name, new Boolean[]{FALSE, TRUE}, Boolean.class);
    }

    @Override
    public int indexOf(Boolean value) {
        return value ? 1 : 0;
    }
}
