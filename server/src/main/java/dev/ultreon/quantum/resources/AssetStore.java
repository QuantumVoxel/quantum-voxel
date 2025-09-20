package dev.ultreon.quantum.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import dev.ultreon.quantum.GamePlatform;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AssetStore implements Disposable {
    private static final AssetStore instance = new AssetStore(System.getProperty("quantum.launch.version"));
    private String version;
    private List<Asset> assets;
    private FileHandle assetsDir;
    private List<String> paths;

    public AssetStore(String version) {
        this.version = version;
    }
    
    public void create(FileHandle assetsDir, String version) {
        this.version = version;
        this.assetsDir = assetsDir;
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        json.setUsePrototypes(false);
        Assets assets = json.fromJson(Assets.class, assetsDir.child(this.version + ".json"));
        this.assets = assets.getAssets();
    }

    @Override
    public void dispose() {
        this.assets = null;
        this.assetsDir = null;
        this.paths = null;
    }

    public static class Asset {
        private final String hash;
        private final String path;

        public Asset(String hash, String path) {
            this.hash = hash;
            this.path = path;
        }

        public String getHash() {
            return hash;
        }

        public String getPath() {
            return path;
        }
    }

    public static class Assets {
        private List<Asset> assets = new ArrayList<>();

        public Assets() {
        }

        public List<Asset> getAssets() {
            return assets;
        }

        public void setAssets(List<Asset> assets) {
            this.assets = assets;
        }
    }

    public List<String> getPaths() {
        if (paths != null) {
            return paths;
        }

        paths = new ArrayList<>();
        for (Asset asset : this.assets) {
            String path = asset.getPath();
            paths.add(path);
        }
        return paths;
    }

    public InputStream openResourceStream(String path) throws IOException {
        String replace = path.replace(File.separatorChar, '/');
        if (replace.startsWith("/")) {
            replace = replace.substring(1);
        }

        for (Asset asset : this.assets) {
            if (asset.getPath().equals(replace)) {
                String hash = asset.getHash();
                String firstTwo = hash.substring(0, 2);

                InputStream objects = assetsDir.child("objects").child(firstTwo).child(hash).read();
                return new AutoClosingInputStream(objects);
            }
        }

        throw new IOException("Asset not found: " + path);
    }
    
    public static AssetStore get() {
        return instance;
    }

    private static class AutoClosingInputStream extends InputStream {
        private final InputStream objects;

        public AutoClosingInputStream(InputStream objects) {
            this.objects = objects;

            GamePlatform.get().onClean(this, () -> {
                try {
                    objects.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public int read(byte @NotNull [] b) throws IOException {
            return objects.read(b);
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            return objects.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return objects.skip(n);
        }

        @Override
        public int available() throws IOException {
            return objects.available();
        }

        @Override
        public void close() throws IOException {
            objects.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            objects.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            objects.reset();
        }

        @Override
        public boolean markSupported() {
            return objects.markSupported();
        }

        @Override
        public int read() {
            return 0;
        }
    }
}
