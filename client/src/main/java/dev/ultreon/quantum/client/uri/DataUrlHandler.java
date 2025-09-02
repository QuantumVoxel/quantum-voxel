package dev.ultreon.quantum.client.uri;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class DataUrlHandler extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!protocol.equals("data")) {
            return null;
        }
        return new DataURL();
    }

}
