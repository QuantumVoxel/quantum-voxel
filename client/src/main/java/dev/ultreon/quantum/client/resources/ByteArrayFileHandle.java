package dev.ultreon.quantum.client.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class represents a file handle that stores data in a byte array.
 * It extends the FileHandle class and overrides its read and readBytes methods.
 * The equals and hashCode methods are also overridden to compare the byte arrays.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class ByteArrayFileHandle extends FileHandle {
    /**
     * The extension of the file.
     */
    private final String extension;

    /**
     * The byte array that holds the data.
     * 
     * @see #ByteArrayFileHandle(String, byte[])
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
        this.extension = extension;
        this.data = data;
    }

    /**
     * Constructor that takes a byte array.
     * The extension string is used to generate a unique filename.
     *
     * @param data The byte array that holds the data.
     */
    public ByteArrayFileHandle(byte[] data) {
        this("", data);
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
     * Overrides the readString method of FileHandle to return a string representation of the byte array.
     *
     * @return A string representation of the byte array.
     */
    @Override
    public String readString() {
        return new String(this.data);
    }

    /**
     * Overrides the write method of FileHandle to write the byte array to the file.
     *
     * @param data The byte array to write to the file.
     */
    public void writeBytes(byte[] data) {
        throw new GdxRuntimeException("Cannot write to a ByteArrayFileHandle");
    }

    /**
     * Overrides the write method of FileHandle to write the byte array to the file.
     *
     * @param data The byte array to write to the file.
     */
    public void writeString(String data) {
        throw new GdxRuntimeException("Cannot write to a ByteArrayFileHandle");
    }

    /**
     * Overrides the length method of FileHandle to return the length of the byte array.
     *
     * @return The length of the byte array.
     */
    @Override
    public long length() {
        return this.data.length;
    }

    /** 
     * Overrides the exists method of FileHandle to return true.
     *
     * @return True.
     */
    @Override
    public boolean exists() {
        return true;
    }
    
    /**
     * Overrides the isDirectory method of FileHandle to return false.
     *
     * @return False.
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /**
     * Overrides the name method of FileHandle to return the name of the file.
     *
     * @return The name of the file.
     */
    @Override
    public String name() {
        return "__generated__" + extension;
    }

    /** 
     * Overrides the nameWithoutExtension method of FileHandle to return the name of the file without the extension.
     *
     * @return The name of the file without the extension.
     */
    @Override
    public String nameWithoutExtension() {
        return "__generated__";
    }

    /**
     * Overrides the extension method of FileHandle to return an empty string.
     *
     * @return An empty string.
     */
    @Override
    public String extension() {
        return extension;
    }
    
    /** 
     * Overrides the path method of FileHandle to return the path of the file.
     *
     * @return The path of the file.
     */
    @Override
    public String path() {
        return "/__generated__" + extension;
    }

    /**
     * Overrides the parent method of FileHandle to return null.
     *
     * @return Null.
     */
    @Override
    public FileHandle parent() {
        return null;
    }

    /**
     * Overrides the list method of FileHandle to return an empty array.
     *
     * @return An empty array.
     */
    @Override
    public FileHandle[] list() {
        return new FileHandle[0];
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
