package dev.ultreon.quantum.client.uri;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.client.QuantumClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class GdxExternalUrlHandler extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!protocol.equals("gdx-external")) {
            return null;
        }
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                FileHandle external = QuantumClient.data(u.getPath());
                return new URLConnection(u) {
                    private InputStream inputStream;
                    private OutputStream outputStream;

                    @Override
                    public void connect() {
                        this.inputStream = external.read();
                        this.outputStream = external.write(false);
                    }

                    @Override
                    public boolean getDoInput() {
                        return true;
                    }

                    @Override
                    public boolean getDoOutput() {
                        return true;
                    }

                    @Override
                    public InputStream getInputStream() {
                        return inputStream;
                    }

                    @Override
                    public OutputStream getOutputStream() {
                        return outputStream;
                    }
                };
            }
        };
    }
}
