package dev.ultreon.quantum.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.ubo.DataIo;
import dev.ultreon.quantum.ubo.types.DataType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.vec.RegionVec;

import java.io.IOException;

/**
 * The WorldStorage class represents a storage system for world data.
 * It provides methods to read, write, and manage data within a given world directory.
 */
public final class WorldStorage {
    /**
     * -- GETTER --
     *
     * @return the world directory.
     */
    private final FileHandle directory;
    private final FileHandle infoFile;
    private boolean infoLoaded;
    private WorldSaveInfo info;
    private String md5Name;
    private String name;

    /**
     * Creates a new world storage instance from the given directory.
     *
     * @param path the world directory.
     */
    public WorldStorage(FileHandle path) {
        this.directory = path;
        this.infoFile = this.getDirectory().child("info.ubo");
    }

    /**
     * Read a UBO object from the given path.
     *
     * @param path       the path to the UBO object.
     * @param typeGetter the type getter. <span style="color: red;">NOTE: do not use this parameter! Leave it empty.</span>
     * @param <T>        the type of the UBO object.
     * @return the UBO object.
     * @throws IOException if an I/O error occurs.
     */
    @SafeVarargs
    public final <T extends DataType<?>> T read(String path, T... typeGetter) throws IOException {
        return DataIo.read(directory.child(path).read(), typeGetter);
    }

    /**
     * Write a UBO object to the given path.
     *
     * @param data the UBO object to write.
     * @param path the path to the UBO object.
     * @throws IOException if an I/O error occurs.
     */
    public void write(DataType<?> data, String path) throws IOException {
        if (!directory.exists()) directory.mkdirs();
        FileHandle child = directory.child(path);
        FileHandle parent = child.parent();
        parent.mkdirs();
        DataIo.write(data, child.write(false));
    }

    /**
     * Check if the given path exists.
     *
     * @param path the path to the UBO object.
     * @return {@code true} if the path exists, {@code false} otherwise.
     */
    public boolean exists(String path) {
        return this.directory.child(path).exists();
    }

    /**
     * Creates a new subdirectory in the world directory.
     *
     * @param path the relative path to the subdirectory.
     */
    public void createDir(String path) {
        // Create the directory if it doesn't exist
        if (Gdx.files.local(path).exists()) {
            return;
        }

        Gdx.files.local(path).mkdirs();
    }

    /**
     * Check if the given region file exists.
     *
     * @param x the x coordinate of the region.
     * @param z the z coordinate of the region.
     * @return {@code true} if the region file exists, {@code false} otherwise.
     */
    public boolean regionExists(int x, int y, int z) {
        return this.exists("regions/" + x + "." + y + "." + z + ".ubo");
    }

    /**
     * Get the region file for the given coordinates.
     *
     * @param x the x coordinate of the region.
     * @param z the z coordinate of the region.
     * @return the region file.
     */
    public FileHandle regionFile(int x, int y, int z) {
        return this.getDirectory().child("regions/" + x + "." + y + "." + z + ".qvr");
    }

    /**
     * Delete the world directory.
     *
     * @return {@code true} if the world directory existed before, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public boolean delete() throws IOException {
        try {
            if (!this.getDirectory().exists()) return false;
            if (GamePlatform.get().isWeb()) {
                deleteDir(this.getDirectory());
                this.getDirectory().deleteDirectory();
                return false;
            }
            this.getDirectory().emptyDirectory(false);
            this.getDirectory().deleteDirectory();
        } catch (GdxRuntimeException e) {
            CommonConstants.LOGGER.error("Failed to delete world directory: " + this.getDirectory().path() + " (" + e.getMessage() + ")");
            return false;
        }
        return true;
    }

    private void deleteDir(FileHandle directory) throws IOException {
        for (FileHandle file : directory.list()) {
            if (file.isDirectory()) {
                deleteDir(file);
                file.deleteDirectory();
            } else {
                file.delete();
            }
        }
    }

    /**
     * Get the region file for the given coordinates.
     *
     * @param pos the position of the region.
     * @return the region file.
     */
    public FileHandle regionFile(RegionVec pos) {
        return this.regionFile(pos.getIntX(), pos.getIntY(), pos.getIntZ());
    }

    /**
     * Create the world directory if it doesn't exist.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void createWorld() throws IOException {
        this.createDir("regions");
        this.createDir("data");
    }

    public WorldSaveInfo loadInfo() {
        if (!this.infoLoaded) {
            this.infoLoaded = true;
            MapType infoData;
            try {
                infoData = DataIo.read(this.infoFile.read());
            } catch (Exception e) {
                infoData = new MapType();
            }

            this.info = WorldSaveInfo.fromMap(infoData);
        }

        return this.info;
    }

    public boolean hasInfo() {
        return this.getDirectory().child("info.ubo").exists();
    }

    /**
     * Retrieves the MD5 hash of the world storage directory name.
     * If the hash has not been computed yet, it generates the hash and stores it.
     *
     * @return The MD5 hash of the world storage directory name in hexadecimal format.
     */
    public String getMD5Name() {
        if (md5Name == null) {
            return getDirectory().name();
        }

        return md5Name;
    }

    /**
     * Generates a unique folder name using the current system time and MD5 hashing.
     *
     * @return A string representing the generated folder name in hexadecimal format.
     */
    public static String createFolderName() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Converts an array of bytes into its hexadecimal string representation.
     *
     * @param bytes an array of bytes to be converted to a hexadecimal string.
     * @return a string representing the hexadecimal value of the byte array.
     */
    public static String bytes2hex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Retrieves the name associated with the world storage.
     * If the name is not already known, it attempts to load this information.
     * If the information cannot be loaded, it defaults to the name of the directory.
     *
     * @return The name of the world storage.
     */
    public String getName() {
        if (name != null) return name;
        if (this.hasInfo()) {
            this.info = loadInfo();
            name = this.info.name();
        } else {
            name = getDirectory().name();
        }
        return name;
    }

    /**
     * Saves the given WorldSaveInfo object and persists the changes.
     *
     * @param worldSaveInfo the WorldSaveInfo object that contains the information to be saved
     * @throws IOException if an I/O error occurs during the saving process
     */
    public void saveInfo(WorldSaveInfo worldSaveInfo) throws IOException {
        this.info = worldSaveInfo;
        if (!this.getDirectory().exists()) {
            this.getDirectory().mkdirs();
        }
        worldSaveInfo.save(this);
    }

    public FileHandle getDirectory() {
        return directory;
    }
}
