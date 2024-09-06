package dev.ultreon.quantum.client;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

/**
 * This class represents the metadata of the application.
 * It holds the version of the application.
 */
public class Metadata {

    /**
     * The singleton instance of the Metadata class.
     */
    private static Metadata instance;

    /**
     * The version of the application.
     * This field is annotated with @SerializedName to specify the name of the field when serializing/deserializing to/from JSON.
     */
    @SerializedName("version")
    public String version;

    /**
     * Returns the singleton instance of the Metadata class.
     * If the instance has not been loaded yet, it loads it from the metadata.json file.
     *
     * @return the singleton instance of the Metadata class
     */
    public static Metadata get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    /**
     * Loads the metadata from the metadata.json file.
     * It uses the Gson library to deserialize the JSON data into a Metadata object.
     * If an IOException occurs while reading the file, it throws an AssertionError.
     *
     * @return the loaded Metadata object
     * @throws AssertionError if an IOException occurs while reading the file
     */
    private static Metadata load() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(Metadata.class.getResourceAsStream("/metadata.json")))) {
            Metadata.instance = QuantumClient.GSON.fromJson(reader, Metadata.class);
            return Metadata.instance;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
