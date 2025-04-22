package dev.ultreon.quantum.api;

import com.badlogic.gdx.files.FileHandle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileIO {
    private FileIO() {
    }

    public static String readString(FileHandle path, Charset charset) throws IOException {
        return new String(path.readBytes(), charset);
    }

    public static byte[] readBytes(FileHandle path) throws IOException {
        try {
            if (!path.exists()) {
                throw new FileNotFoundException(path.path());
            }
            return path.readBytes();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String readString(FileHandle resolve) throws IOException {
        return readString(resolve, Charset.defaultCharset());
    }

    public static void writeString(FileHandle path, CharSequence str, Charset charset) {
        path.writeString(str.toString(), false, charset.toString());
    }

    public static void writeString(FileHandle path, CharSequence str) {
        writeString(path, str, Charset.defaultCharset());
    }

    public static void writeBytes(FileHandle path, byte[] bytes) {
        path.writeBytes(bytes, false);
    }
}
