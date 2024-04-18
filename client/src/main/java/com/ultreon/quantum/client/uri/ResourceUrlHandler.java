package com.ultreon.quantum.client.uri;

import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.resources.Resource;
import com.ultreon.quantum.resources.ResourceManager;
import com.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class ResourceUrlHandler extends URLStreamHandlerProvider {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!protocol.equals("res")) {
            return null;
        }
        return new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                String location = u.getHost();
                String path = u.getPath();
                Identifier identifier = new Identifier(location, path);
                QuantumClient quantumClient = QuantumClient.get();
                return new URLConnection(u) {
                    private ResourceManager resourceManager;

                    private InputStream inputStream;
                    @Override
                    public void connect() throws IOException {
                        if (quantumClient == null)
                            throw new IOException("Connection opened before game initialization");

                        this.resourceManager = quantumClient.getResourceManager();

                        if (this.resourceManager == null)
                            throw new IOException("Connection opened before game initialization");

                        @Nullable Resource resource = this.resourceManager.getResource(identifier);

                        if (resource == null)
                            throw new FileNotFoundException("Resource not found: " + identifier);

                        InputStream stream = resource.openStream();

                        if (stream == null)
                            throw new IOException("Failed to load or open the resource stream");

                        this.inputStream = stream;
                    }

                    @Override
                    public boolean getDoInput() {
                        return true;
                    }

                    @Override
                    public boolean getDoOutput() {
                        return false;
                    }

                    @Override
                    public InputStream getInputStream() {
                        return inputStream;
                    }

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        throw new IOException("Resource urls are read-only.");
                    }

                    public ResourceManager getResourceManager() {
                        return resourceManager;
                    }
                };
            }
        };
    }
}
