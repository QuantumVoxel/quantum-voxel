package dev.ultreon.quantum.desktop.darwin.foundation;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.sun.jna.Pointer;
import dev.ultreon.quantum.desktop.darwin.ObjC;
import dev.ultreon.quantum.desktop.darwin.ObjCObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class NSImage extends ObjCObject {
    private final Pointer pointer;

    public NSImage() throws IOException {
        // Call NSImage::alloc
        Pointer nsImageClass = ObjC.INSTANCE.objc_getClass("NSImage");
        Pointer allocSelector = ObjC.INSTANCE.sel_getUid("alloc");
        pointer = ObjC.INSTANCE.objc_msgSend(nsImageClass, allocSelector);

        if (pointer.equals(Pointer.NULL)) {
            throw new IOException("Failed to allocate an NSImage instance.");
        }
    }

    public NSImage(NSString imagePath) throws IOException {
        this();
        initWithContentsOfFile(imagePath);
    }

    /**
     * Creates a new NSImage instance
     *
     * @param imagePath the absolute path to the image.
     */
    public NSImage(String imagePath) throws IOException {
        this();
        initWithContentsOfFile(imagePath);
    }

    public NSImage(FileHandle imagePath) throws IOException {
        this();
        initWithContentsOfFile(imagePath);
    }

    public NSImage(Path imagePath) throws IOException {
        this();
        initWithContentsOfFile(imagePath);
    }

    public NSImage(Pointer pointer) {
        this.pointer = pointer;
    }

    private static Path __extractFile(FileHandle imagePath) throws IOException {
        Path temp = Files.createTempDirectory("java");
        String name = imagePath.name();

        InputStream read = imagePath.read();
        Path copied = temp.resolve(name);
        return __writeTo(read, copied);
    }

    private static @NotNull Path __writeTo(InputStream read, Path copied) throws IOException {
        Files.copy(read, copied);

        return copied;
    }

    @Override
    public Pointer getPointer() {
        return pointer;
    }

    public void initWithContentsOfFile(NSString imagePath) throws IOException {
        // Call NSImage::initWithContentsOfFile(imagePath)
        Pointer pointer1 = __msgSend("initWithContentsOfFile:", imagePath.getPointer());

        if (pointer1.equals(Pointer.NULL)) {
            throw new IOException("Failed to initialize an NSImage instance.");
        }
    }

    public void initWithContentsOfFile(String imagePath) throws IOException {
        initWithContentsOfFile(new NSString(imagePath));
    }

    public void initWithContentsOfFile(Path imagePath) throws IOException {
        initWithContentsOfFile(new NSString(imagePath.toAbsolutePath().toString()));
    }

    public void initWithContentsOfFile(FileHandle imagePath) throws IOException {
        FileType type = imagePath.type();
        Path path;
        if (type == FileType.Internal) {
            path = __extractFile(imagePath);
        } else {
            path = imagePath.file().toPath();
        }

        initWithContentsOfFile(path.toAbsolutePath().toString());
    }

    public void initWithContentsOfFile(InputStream inputStream) throws IOException {
        Path path = __writeTo(inputStream, Files.createTempFile("java", ".icns"));
        initWithContentsOfFile(path);
    }
}
