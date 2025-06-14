package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Represents a screenshot grabbed from the game.
 *
 * @since 0.1.0
 * @see Pixmap
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class Screenshot {
    private final Pixmap pixmap;
    private boolean disposed = false;

    /**
     * Constructs a new Screenshot object with the given Pixmap.
     *
     * @param pixmap the Pixmap to be used for the screenshot
     */
    public Screenshot(Pixmap pixmap) {
        this.pixmap = pixmap;
    }

    /**
     * Gets the Pixmap associated with this screenshot.
     *
     * @return the Pixmap
     */
    public Pixmap getPixmap() {
        return pixmap;
    }

    /**
     * Disposes of the resources held by the object.
     */
    public void dispose() {
        // Marking the object as disposed
        this.disposed = true;

        // Disposing of the pixmap resource
        this.pixmap.dispose();
    }

    /**
     * Check if the object is disposed.
     *
     * @return true if the object is disposed, false otherwise
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * Saves the given filename to a file handle and returns the file handle.
     *
     * @param filename The name of the file to be saved.
     * @return The file handle of the saved file.
     */
    public FileHandle save(FileHandle filename) {
        // Write the data to a PNG file
        PixmapIO.writePNG(filename, pixmap);

        // Copy the screenshot file to clipboard
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            IClipboard clipboard = QuantumClient.get().clipboard;
            try(InputStream read = filename.read()) {
//                clipboard.copy(ImageIO.read(read));
            } catch (IOException e) {
                QuantumClient.LOGGER.error("Failed to copy screenshot to clipboard", e);
            }
        }

        return filename;
    }


    /**
     * Saves the data to a file with the given filename and disposes of any resources.
     *
     * @param filename The name of the file to save the data to
     * @return The FileHandle object representing the saved file
     */
    public FileHandle saveAndDispose(String filename) {
        FileHandle data = save(Gdx.files.local("screenshots/" + filename));
        dispose();

        return data;
    }
    /**
     * Takes a screenshot of the current frame with the specified width and height.
     *
     * @param width  The width of the screenshot
     * @param height The height of the screenshot
     * @return A Screenshot object representing the captured image
     */
    public static Screenshot grab(int width, int height) {
        GridPoint2 drawOffset = QuantumClient.get().getDrawOffset();
        width -= drawOffset.x * 2;
        height -= drawOffset.y * 2;

        // Set the pixel store alignment
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

        // Calculate the length of the pixel data
        int dataLen = width * height * 4;
        final ByteBuffer pixels = BufferUtils.newByteBuffer(dataLen);

        // Read the pixel data from the frame buffer
        Gdx.gl.glReadPixels(drawOffset.x, drawOffset.y, width, height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);

        // Rearrange the pixel data to match the image format
        byte[] lines = new byte[dataLen];
        final int numBytesPerLine = width * 4;
        for (int i = 0; i < height; i++) {
            ((Buffer) pixels).position((height - i - 1) * numBytesPerLine);
            pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
        }

        // Create a Pixmap and copy the pixel data to it
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        BufferUtils.copy(lines, 0, pixmap.getPixels(), lines.length);

        // Return the captured screenshot as a Screenshot object
        return new Screenshot(pixmap);
    }
}
