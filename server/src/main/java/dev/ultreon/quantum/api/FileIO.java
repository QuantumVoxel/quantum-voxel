package dev.ultreon.quantum.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileIO {
    private FileIO() {
    }

    public static String readString(Path path, Charset charset) throws IOException {
        return new String(readBytes(path), charset);
    }

    public static byte[] readBytes(Path path) throws IOException {
        try {
            FileHandle absolute = Gdx.files.absolute(path.toString());

            if (!absolute.exists()) {
                throw new FileNotFoundException(absolute.path());
            }
            return absolute.readBytes();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String readString(Path resolve) throws IOException {
        return readString(resolve, Charset.defaultCharset());
    }

    public static void writeString(Path path, CharSequence str, Charset charset) {
        FileHandle local = Gdx.files.local(path.toString());
        local.writeString(str.toString(), false, charset.toString());
    }

    public static void writeString(Path path, CharSequence str) {
        writeString(path, str, Charset.defaultCharset());
    }

    public static void writeBytes(Path path, byte[] bytes) {
        FileHandle local = Gdx.files.local(path.toString());
        local.writeBytes(bytes, false);
    }
}
