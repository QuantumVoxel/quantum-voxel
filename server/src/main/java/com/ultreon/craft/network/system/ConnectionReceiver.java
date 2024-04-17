package com.ultreon.craft.network.system;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public interface ConnectionReceiver extends Closeable {
    Socket accept() throws IOException;
}
