package dev.ultreon.quantum.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Os;
import dev.ultreon.libs.commons.v0.Logger;
import dev.ultreon.libs.commons.v0.exceptions.SyntaxException;
import dev.ultreon.libs.commons.v0.util.IOUtils;
import dev.ultreon.libs.functions.v0.misc.ThrowingSupplier;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.events.ResourceEvent;
import dev.ultreon.quantum.resources.android.DeferredResourcePackage;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceManager extends GameObject implements Closeable {
    protected final List<ResourcePackage> resourcePackages = new ArrayList<>();
    public static Logger logger = (level, msg, t) -> {
    };
    private final String root;

    public ResourceManager(String root) {
        this.root = root;
    }

    public boolean canScanFiles() {
        return true;
    }

    public InputStream openResourceStream(NamespaceID entry) throws IOException {
        @Nullable Resource resource = this.getResource(entry);
        return resource == null ? null : resource.openStream();
    }

    @Nullable
    public Resource getResource(NamespaceID entry) {
        for (ResourcePackage resourcePackage : this.resourcePackages) {
            if (resourcePackage.has(entry)) {
                return resourcePackage.get(entry);
            }
        }

        logger.warn("Unknown resource: " + entry);
        return null;
    }

    public void importDeferredPackage(Class<?> ref) {
        addImported(new DeferredResourcePackage(ref, this.root));
    }

    public void importPackage(URI uri) throws IOException {
        URL url = uri.toURL();
        if (url.getProtocol().equals("file")) {
            this.importPackage(new FileHandle(new File(uri)));
        } else if (url.getProtocol().equals("jar")) {
            try {
                this.importFilePackage(new ZipInputStream(new URI(uri.toURL().getPath().split("!/", 2)[0]).toURL().openStream()), uri.toASCIIString());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI: " + uri, e);
            }
        } else {
            this.importFilePackage(new ZipInputStream(uri.toURL().openStream()), uri.toASCIIString());
        }
    }

    public void importPackage(FileHandle file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Resource package doesn't exists: " + file.path());
        }

        if (!file.isDirectory()) {
            if (file.name().endsWith(".jar") || file.name().endsWith(".zip")) {
                this.importFilePackage(new ZipInputStream(file.read()), file.path());
            } else {
                logger.warn("Resource package isn't a .jar or .zip file: " + file.path());
            }
        } else if (file.isDirectory()) {
            this.importDirectoryPackage(file);
        }
    }

    public void loadFromAssetsTxt(FileHandle internal) {
        String[] fileList = internal.readString().split("\n");
        if (fileList.length == 0) {
            logger.warn("No files in assets.txt: " + internal.path());
            return;
        }

        // Prepare mappings
        Map<NamespaceID, StaticResource> map = new HashMap<>();
        Map<String, ResourceCategory> categories = new HashMap<>();

        for (String file : fileList) {
            if (file.startsWith(root + "/")) {
                String domain = file.substring(root.length() + 1);
                String domainId = domain.substring(0, domain.indexOf('/'));
                String[] path = domain.substring(domain.indexOf('/') + 1).split("/");
                String[] categoryParts = Arrays.copyOf(path, path.length - 1);
                String filename = path[path.length - 1];

                String categoryPath = categoryParts.length > 0 ? String.join("/", categoryParts) + "/" : "";
                String filePath = categoryPath + filename;

                StaticResource resource = new StaticResource(
                        new NamespaceID(domainId, filePath),
                        () -> Gdx.files.internal(file).read()
                );

                // Add to categories map
                if (categoryParts.length > 0) {
                    String category = categoryParts[0];
                    categories.computeIfAbsent(category, ResourceCategory::new)
                            .set(new NamespaceID(domainId, filePath), resource);
                }

                // Add to resources map
                map.put(new NamespaceID(domainId, filePath), resource);
            }
        }

        addImported(new ResourcePackage(map, categories));
    }
    
    @SuppressWarnings({"unused"})
    private void importDirectoryPackage(FileHandle file) {
        // Check if it's a directory.
        assert file.isDirectory();

        // Prepare (entry -> resource) mappings
        Map<NamespaceID, StaticResource> map = new HashMap<>();

        // Resource categories
        Map<String, ResourceCategory> categories = new HashMap<>();

        // Get assets directory.
        FileHandle assets = file.child(this.root);

        // Check if the assets directory exists.
        if (assets.exists()) {
            // List files in assets dir.
            FileHandle[] files = assets.list();

            // Loop listed files.
            for (FileHandle resPackage : files != null ? files : new FileHandle[0]) {
                // Get assets-package namespace from the name create the listed file (that's a dir).
                String namespace = resPackage.name();

                // Walk assets package.
                try (Stream<FileHandle> walk = walk(resPackage)) {
                    for (FileHandle assetPath : walk.collect(Collectors.toList())) {
                        // Convert to a file object.

                        // Check if it's a file, if not,
                        // we will walk to the next file / folder in the Files.walk(...)
                        // list.
                        if (assetPath.isDirectory()) {
                            continue;
                        }

                        // Continue to the next file / folder
                        // if the asset path is the same path as the resource package.
                        if (assetPath.equals(resPackage)) {
                            continue;
                        }

                        // Calculate resource path.
                        FileHandle relative = Gdx.files.getFileHandle(resPackage.path().substring(assets.path().length() + 1), resPackage.type());
                        String s = relative.toString().replaceAll("\\\\", "/");

                        // Create resource entry/
                        NamespaceID entry;
                        try {
                            entry = new NamespaceID(namespace, s);
                        } catch (SyntaxException e) {
                            logger.error("Invalid resource identifier:", e);
                            continue;
                        }

                        // Create resource with file input stream.
                        ThrowingSupplier<InputStream, IOException> sup = assetPath::read;
                        StaticResource resource = new StaticResource(entry, sup);

                        String path = entry.getPath();
                        String[] split = path.split("/");
                        String category = split[0];
                        if (split.length > 1) {
                            categories.computeIfAbsent(category, ResourceCategory::new).set(entry, resource);
                        }

                        // Add resource mapping for (entry -> resource).
                        map.put(entry, resource);
                    }
                }
            }

            addImported(new ResourcePackage(map, categories));
        }
    }

    private Stream<FileHandle> walk(FileHandle resPackage) {
        if (!resPackage.exists()) {
            return Stream.empty();
        }

        List<FileHandle> files = new ArrayList<>();
        files.add(resPackage);

        if (resPackage.isDirectory()) {
            for (FileHandle child : resPackage.list()) {
                files.addAll(walk(child).collect(Collectors.toList()));
            }
        }

        return files.stream();
    }

    private void importFilePackage(ZipInputStream stream, String filePath) throws IOException {
        // Check for .jar files.
        // Prepare (entry -> resource) mappings.
        Map<NamespaceID, StaticResource> map = new HashMap<>();

        // Resource categories
        Map<String, ResourceCategory> categories = new HashMap<>();

        // Create jar file instance from file.
        try {
            // Loop jar entries.
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                // Get name to create the jar entry.
                String name = entry.getName();
                byte[] bytes = IOUtils.readAllBytes(stream);
                ThrowingSupplier<InputStream, IOException> sup = () -> new ByteArrayInputStream(bytes);

                // Check if it isn't a directory, because we want a file.
                if (!entry.isDirectory()) {
                    this.addEntry(map, categories, name, sup);
                }
                stream.closeEntry();
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load resource package: {}", filePath, e);
        }

        addImported(new ResourcePackage(map, categories));

        stream.close();
    }

    private void addImported(ResourcePackage pkg) {
        this.resourcePackages.add(pkg);
        this.add(pkg.getName(), pkg);
        ResourceEvent.IMPORTED.factory().onImported(pkg);
    }

    private void addEntry(Map<NamespaceID, StaticResource> map, Map<String, ResourceCategory> categories, String name, ThrowingSupplier<InputStream, IOException> sup) {
        String[] splitPath = name.split("/", 3);

        if (splitPath.length >= 3) {
            if (name.startsWith(this.root + "/")) {
                // Get namespace and path from the split path
                String namespace = splitPath[1];
                String path = splitPath[2];

                // Entry
                NamespaceID entry = new NamespaceID(namespace, path);

                // Resource
                StaticResource resource = new StaticResource(entry, sup);

                // Category
                String[] split = path.split("/");
                String category = split[0];
                if (split.length > 1) {
                    categories.computeIfAbsent(category, ResourceCategory::new).set(entry, resource);
                }

                try {

                    // Add (entry -> resource) mapping.
                    map.put(entry, resource);
                } catch (Throwable ignored) {

                }
            }
        }
    }

    @NotNull
    public List<byte[]> getAllDataByPath(@NotNull String path) {
        List<byte[]> data = new ArrayList<>();
        for (ResourcePackage resourcePackage : this.resourcePackages) {
            Map<NamespaceID, StaticResource> identifierResourceMap = resourcePackage.mapEntries();
            for (Map.Entry<NamespaceID, StaticResource> entry : identifierResourceMap.entrySet()) {
                if (entry.getKey().getPath().equals(path)) {
                    byte[] bytes = entry.getValue().loadOrGet();
                    if (bytes == null) continue;

                    data.add(entry.getValue().getData());
                }
            }
        }

        return data;
    }

    @NotNull
    public List<byte[]> getAllDataById(@NotNull NamespaceID id) {
        List<byte[]> data = new ArrayList<>();
        for (ResourcePackage resourcePackage : this.resourcePackages) {
            if (resourcePackage.has(id)) {
                StaticResource resource = resourcePackage.get(id);
                if (resource == null) continue;
                byte[] bytes = resource.loadOrGet();
                if (bytes == null) continue;

                data.add(resource.getData());
            }
        }

        return data;
    }

    public String getRoot() {
        return this.root;
    }

    public List<ResourceCategory> getResourceCategory(String category) {
        return this.resourcePackages.stream().map(resourcePackage -> {
            if (!resourcePackage.hasCategory(category)) {
                return null;
            }

            return resourcePackage.getCategory(category);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ResourcePackage> getResourcePackages() {
        return Collections.unmodifiableList(this.resourcePackages);
    }

    public List<ResourceCategory> getResourceCategories() {
        return this.resourcePackages.stream().flatMap(resourcePackage -> resourcePackage.getCategories().stream()).collect(Collectors.toList());
    }

    public void close() {
        for (ResourcePackage resourcePackage : this.resourcePackages) {
            resourcePackage.close();
        }
    }

    public void reload() {
        for (ResourcePackage resourcePackage : this.resourcePackages) {
            this.remove(resourcePackage);
            resourcePackage.close();
        }

        this.resourcePackages.clear();

        this.discover();
    }

    private void discover() {
        this.importGameResources();
        this.importModResources();

        try {
            this.importResourcePackages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importResourcePackages() throws IOException {
        FileHandle dir;
        if (GamePlatform.get().isMobile() || GamePlatform.get().isWeb()) {
            dir = Gdx.files.local("resource-packages");
        } else {
            dir = GamePlatform.get().getGameDir().child("resource-packages");
        }
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        FileHandle[] list = dir.list();
        for (FileHandle fileHandle : list) {
            this.importFrom(fileHandle);
        }
    }

    private void importFrom(FileHandle list) {
        try {
            this.importPackage(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importGameResources() {
        GamePlatform.get().locateResources();
    }

    public void importModResources() {
        GamePlatform.get().locateModResources();
    }
}
