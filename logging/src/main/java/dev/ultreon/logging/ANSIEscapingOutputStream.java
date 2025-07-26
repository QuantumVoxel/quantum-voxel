package dev.ultreon.logging;

import java.io.IOException;
import java.io.OutputStream;

class ANSIEscapingOutputStream extends OutputStream {
    private final OutputStream delegate;
    private boolean prefixing;
    private boolean escaping;

    public ANSIEscapingOutputStream(OutputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\u001B') {
            prefixing = true;
        } else if (b == '[' && prefixing) {
            prefixing = false;
            escaping = true;
        } else if (!escaping || prefixing) {
            if (prefixing) delegate.write('\u001B');
            delegate.write(b);
        } else if (b == 'm') {
            escaping = false;
        }
    }
}
