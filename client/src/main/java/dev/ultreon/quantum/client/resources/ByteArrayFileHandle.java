package dev.ultreon.quantum.client.resources;

import com.badlogic.gdx.files.FileHandle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class represents a file handle that stores data in a byte array.
 * It extends the FileHandle class and overrides its read and readBytes methods.
 * The equals and hashCode methods are also overridden to compare the byte arrays.
 *
 * @author Your Name
 */
public class ByteArrayFileHandle extends FileHandle {

    /**
     * The byte array that holds the data.
     */
    private final byte[] data;

    /**
     * Constructor that takes an extension string and a byte array.
     * The extension string is used to generate a unique filename.
     *
     * @param extension The extension string used to generate a unique filename.
     * @param data The byte array that holds the data.
     */
    public ByteArrayFileHandle(String extension, byte[] data) {
        super("generated " + UUID.randomUUID() + extension);
        this.data = data;
    }

    /**
     * Overrides the read method of FileHandle to return an InputStream that reads from the byte array.
     *
     * @return An InputStream that reads from the byte array.
     */
    @Override
    public InputStream read() {
        return new ByteArrayInputStream(this.data);
    }

    /**
     * Overrides the readBytes method of FileHandle to return a clone of the byte array.
     *
     * @return A clone of the byte array.
     */
    @Override
    public byte[] readBytes() {
        return this.data.clone();
    }

    /**
     * Overrides the equals method of FileHandle to compare the byte arrays.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ByteArrayFileHandle that = (ByteArrayFileHandle) o;
        return Arrays.equals(this.data, that.data);
    }

    /**
     * Overrides the hashCode method of FileHandle to return a hash code based on the byte array.
     *
     * @return The hash code based on the byte array.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(this.data);
        return result;
    }
}
