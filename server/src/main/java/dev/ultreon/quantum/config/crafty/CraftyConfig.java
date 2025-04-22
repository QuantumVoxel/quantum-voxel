package dev.ultreon.quantum.config.crafty;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.util.ModLoadingContext;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.*;

/**
 * The base class for all configuration files.
 * Files are stored in the path provided in {@link GamePlatform#getConfigDir()}.
 * Those files are also automatically reloaded when they are modified.
 * Configs are saved in JSON5 format. See {@link com.badlogic.gdx.utils.Json} for more information.
 *
 * @see <a href="https://spec.json5.org/">JSON5 Specification</a>
 * @see com.badlogic.gdx.utils.Json
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public abstract class CraftyConfig {
    public static final String ENTRYPOINT_KEY = "config";
    private static final Map<String, CraftyConfig> CONFIGS = new HashMap<>();

    static {
        // Add a shutdown hook to handle cleanup tasks
        GamePlatform.get().addShutdownHook(() -> {
            // Save all the CraftyConfig instances
            for (CraftyConfig config : CONFIGS.values()) {
                config.save();
            }
        });
    }

    private final Map<String, ConfigEntry> entriesMap;
    private final Map<String, Object> defaultsMap;
    private final Map<String, Class<?>> typesMap;
    private final Map<String, Field> fieldsMap;
    private final Map<String, Ranged> rangesMap;
    private final FileHandle configPath;
    public final Event<LoadConfig> event = Event.create(listeners -> () -> {
        for (LoadConfig listener : listeners) {
            listener.load();
        }
    });

    private final Mod mod;

    /**
     * Constructor for CraftyConfig class.
     * Initializes the configuration based on annotations present in the class fields.
     */
    public CraftyConfig() {

        // Get the class of the config
        Class<? extends CraftyConfig> configClass = getClass();
        // Get the ConfigInfo annotation of the class
        ConfigInfo annotation = configClass.getAnnotation(ConfigInfo.class);

        // Check if the annotation is missing
        if (annotation == null)
            throw new IllegalStateException("Class " + configClass + " is not annotated with @ConfigInfo");

        // Set the file name for the configuration
        this.configPath = GamePlatform.get().getConfigDir().child(annotation.fileName() + ".json5");

        // Get all declared fields of the class
//        Field[] declaredFields = configClass.getDeclaredFields();

        // Initialize maps for configuration entries, defaults, types, fields, and ranges
        entriesMap = new HashMap<>();
        defaultsMap = new HashMap<>();
        typesMap = new HashMap<>();
        fieldsMap = new HashMap<>();
        rangesMap = new HashMap<>();

        // Process each field for configuration entry annotation
//        for (Field field : declaredFields) {
//            if (field.isAnnotationPresent(ConfigEntry.class)) {
//                // Check field modifiers
//                if (!Modifier.isPublic(field.getModifiers()))
//                    throw new IllegalStateException("Field " + field + " is not public but is annotated with @ConfigEntry");
//                if (Modifier.isFinal(field.getModifiers()))
//                    throw new IllegalStateException("Field " + field + " is final but is annotated with @ConfigEntry");
//                if (!Modifier.isStatic(field.getModifiers()))
//                    throw new IllegalStateException("Field " + field + " is not static but is annotated with @ConfigEntry");
//
//                // Process the entry
//                processEntry(field);
//            }
//        }

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        this.mod = modLoadingContext.getMod();

        // Add the configuration to the global map
        CONFIGS.put(annotation.fileName() + ".json5", this);
    }

    /**
     * Get the CraftyConfig instance associated with the given file name.
     *
     * @param fileName The name of the file to get the CraftyConfig instance for.
     * @return The CraftyConfig instance associated with the given file name.
     */
    public static CraftyConfig getConfig(String fileName) {
        return CONFIGS.get(fileName);
    }

    /**
     * Reset all CraftyConfig instances.
     */
    public static void resetAll() {
        CONFIGS.values().forEach(CraftyConfig::reset);
    }

    /**
     * Save all CraftyConfig instances.
     */
    public static void saveAll() {
        CONFIGS.values().forEach(CraftyConfig::save);
    }

    /**
     * Load all CraftyConfig instances.
     */
    public static void loadAll() {
        CONFIGS.values().forEach(CraftyConfig::load);
    }

    public static Collection<? extends CraftyConfig> getConfigs() {
        return CONFIGS.values();
    }

    public static Collection<CraftyConfig> getByMod(Mod mod) {
        Set<CraftyConfig> configs = new LinkedHashSet<>();
        for (CraftyConfig config : CONFIGS.values()) {
            if (config.getMod() == mod) {
                configs.add(config);
            }
        }
        return Collections.unmodifiableSet(configs);
    }

    /**
     * Load the configuration from the specified file, replacing any existing values with defaults if necessary.
     *
     * @return true if the configuration was loaded successfully, false otherwise
     * @throws IOException if an I/O error occurs
     */
    protected boolean loadUnsafe() throws IOException {
        // Parse the JSON5 file into a JsonValue
        JsonValue root = CommonConstants.JSON_READ.parse(this.configPath.readString("UTF-8"));

        // Check if the root id is an object
        if (!(root instanceof JsonValue)) {
            throw new GdxRuntimeException("Root id is not an object");
        }

        boolean success = true;
        // Iterate through the defaultsMap entries and update the configuration values
        for (Map.Entry<String, Object> entry : defaultsMap.entrySet()) {
            String path = entry.getKey();
            Object defaultValue = entry.getValue();
            ConfigEntry configEntry = entriesMap.get(path);
            Field field = fieldsMap.get(path);
            Class<?> type = typesMap.get(path);

            // Skip processing if any of the required elements are null
            if (field == null || type == null || configEntry == null) continue;

            try {
                // Get the current value for the config entry
                JsonValue current = getElement(root, path);
                if (current == null) {
                    // Set the default value if the current value is null
                    field.set(null, defaultValue);
                }

                // Parse the current value based on the type and update the field with the parsed value
                Object value = parseValue(current, type, rangesMap.get(path));
                if (configEntry.defaulted() || defaultsMap.get(path) != null) {
                    // Set the value if it's a default or if there's an existing default value
                    field.set(null, value);
                } else {
                    // Set the defaults for the field if necessary
                    this.setDefaults(field, type);
                }
            } catch (IllegalAccessException e) {
                // Throw an exception if there's an access error
                throw new IllegalStateException("Failed to load config entry " + path, e);
            } catch (Exception e) {
                // Log an error and set success to false if an error occurs during processing
                CommonConstants.LOGGER.error("Failed to load config entry {}", path, e);
                success = false;
            }
        }

        event.factory().load();

        return success;
    }

    /**
     * Saves the configurations to a file. Any missing fields will be filled with default values.
     *
     * @throws IOException if an I/O error occurs
     */
    protected void saveUnsafe() throws IOException {
        // Create a JsonValue to hold the configurations
        JsonValue root = new JsonValue(JsonValue.ValueType.object);

        // Iterate through the default configurations
        for (Map.Entry<String, Object> entry : defaultsMap.entrySet()) {
            String path = entry.getKey();
            ConfigEntry configEntry = entriesMap.get(path);
            Field field = fieldsMap.get(path);
            Class<?> type = typesMap.get(path);

            // Skip if any necessary information is missing
            if (field == null || type == null || configEntry == null) continue;

            try {
                // Get the value of the field
                Object value = field.get(null);
                String comment = configEntry.comment();

                // Set the comment based on whether it is blank or not
                if (comment.isBlank()) {
                    comment = "Default value: " + serializeValue(value, type);
                } else {
                    comment += "\n\nDefault value: " + serializeValue(value, type);
                }

                // Serialize the value and add it to the JsonValue
                this.setElement(root, path, value == null ? new JsonValue(JsonValue.ValueType.nullValue) : serializeValue(value, type), comment);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to save config entry " + path, e);
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to save config entry {}", path, e);
            }
        }

        // Write the serialized configurations to a file
        this.configPath.writeString(serialize(root).toString(), false, "UTF-8");
    }

    /**
     * Disables the watcher, saves the file, and then re-enables the watcher.
     */
    public void save() {
        try {
            saveUnsafe();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to save config file {}", this.configPath, e);
        }
    }

    /**
     * Reset the configuration to default values and delete the configuration file.
     */
    public void reset() {
        // Delete the configuration file if it exists or do nothing if it doesn't
        if (!this.configPath.exists()) return;
        this.configPath.delete();

        // Set default values for all configuration fields
        this.fieldsMap.forEach((ignored, field) -> {
            try {
                this.setDefaults(field, field.getType());
            } catch (IllegalAccessException e) {
                // Log an error if setting default value fails
                CommonConstants.LOGGER.error("Failed to reset config entry {}", field.getName(), e);
            }
        });

        // Save the configuration to the file
        this.save();
    }

    /**
     * Serialize a JsonValue to a CharSequence.
     *
     * @param root The JsonValue to serialize
     * @return The serialized JsonValue as a CharSequence
     */
    private CharSequence serialize(JsonValue root) {
        // Create a StringWriter to write the serialized JsonValue
        StringWriter writer = new StringWriter();

        // Return the serialized JsonValue as a CharSequence
        return CommonConstants.JSON5.toJson(root);
    }

    /**
     * Sets the id at the specified path in the JSON object, with an optional comment.
     *
     * @param root    the root JSON object
     * @param path    the path to the id
     * @param value   the value to set
     * @param comment the comment to associate with the id
     */
    @SuppressWarnings({"ConditionCoveredByFurtherCondition"})
    private void setElement(JsonValue root, String path, JsonValue value, String comment) {
        // Split the path into parts
        String[] parts = path.split("\\.");
        JsonValue current = root;

        // Traverse the path and create any missing objects
        for (int i = 0; i < parts.length - 1; i++) {
            JsonValue tempCurrent = current.get(parts[i]);
            if (tempCurrent == null || !(tempCurrent instanceof JsonValue)) {
                // Create a new JSON object if the current id is missing
                JsonValue newValue = new JsonValue(JsonValue.ValueType.object);
                tempCurrent = newValue;
                current.addChild(parts[i], tempCurrent);

                current = newValue;
                continue;
            }
            JsonValue object = tempCurrent;

            current = object;
        }

        // Set the value and associate the comment
        current.addChild(parts[parts.length - 1], value);
//        current.setComment(parts[parts.length - 1], comment);
    }

    /**
     * Serializes the given value based on the provided type and returns the corresponding JsonValue.
     *
     * @param value The value to be serialized.
     * @param type  The type of the value.
     * @return The serialized JsonValue.
     * @throws IllegalStateException if the type is unsupported.
     */
    private JsonValue serializeValue(Object value, Class<?> type) {
        // Serialize based on the type
        if (type == String.class) {
            return new JsonValue((String) value);
        } else if (type == Number.class) {
            return new JsonValue(((Number) value).doubleValue());
        } else if (type == Boolean.class) {
            return new JsonValue((Boolean) value);
        } else if (type == UUID.class) {
            return new JsonValue(value.toString());
        } else if (type == BigInteger.class) {
            return new JsonValue(value.toString());
        } else if (type == BigDecimal.class) {
            return new JsonValue(value.toString());
        } else if (type == Character.class) {
            return new JsonValue(((Character) value).toString());
        } else if (type == Byte.class) {
            return new JsonValue((Byte) value);
        } else if (type == Short.class) {
            return new JsonValue((Short) value);
        } else if (type == Integer.class) {
            return new JsonValue((Integer) value);
        } else if (type == Long.class) {
            return new JsonValue((Long) value);
        } else if (type == Float.class) {
            return new JsonValue((Float) value);
        } else if (type == Double.class) {
            return new JsonValue((Double) value);
        } else if (type == boolean.class) {
            return new JsonValue((boolean) value);
        } else if (type == byte.class) {
            return new JsonValue((byte) value);
        } else if (type == short.class) {
            return new JsonValue((short) value);
        } else if (type == int.class) {
            return new JsonValue((int) value);
        } else if (type == long.class) {
            return new JsonValue((long) value);
        } else if (type == float.class) {
            return new JsonValue((float) value);
        } else if (type == double.class) {
            return new JsonValue((double) value);
        } else if (type == NamespaceID.class) {
            return new JsonValue(((NamespaceID) value).toString());
        } else if (type == JsonValue.class) {
            return (JsonValue) value;
        } else {
            throw new IllegalStateException("Unsupported type " + type);
        }
    }

    /**
     * Sets default values for different types of fields.
     *
     * @param field the field to set the default value for
     * @param type the type of the field
     * @throws IllegalAccessException if the default value cannot be set
     */
    private void setDefaults(Field field, Class<?> type) throws IllegalAccessException {
        if (type == String.class) {
            field.set(null, "");
        } else if (type == Number.class) {
            field.set(null, 0);
        } else if (type == Boolean.class) {
            field.set(null, false);
        } else if (type == UUID.class) {
            field.set(null, UUID.fromString("00000000-0000-0000-0000-000000000000"));
        } else if (type == BigInteger.class) {
            field.set(null, BigInteger.ZERO);
        } else if (type == BigDecimal.class) {
            field.set(null, new BigDecimal(0));
        } else if (type == Character.class) {
            field.set(null, (char) 0x20);
        } else if (type == Byte.class) {
            field.set(null, (byte) 0);
        } else if (type == Short.class) {
            field.set(null, (short) 0);
        } else if (type == Integer.class) {
            field.set(null, 0);
        } else if (type == Long.class) {
            field.set(null, 0L);
        } else if (type == Float.class) {
            field.set(null, 0F);
        } else if (type == Double.class) {
            field.set(null, 0D);
        } else if (type == boolean.class) {
            field.set(null, false);
        } else if (type == char.class) {
            field.set(null, (char) 0x20);
        } else if (type == byte.class) {
            field.set(null, (byte) 0);
        } else if (type == short.class) {
            field.set(null, (short) 0);
        } else if (type == int.class) {
            field.set(null, 0);
        } else if (type == long.class) {
            field.set(null, 0L);
        } else if (type == float.class) {
            field.set(null, 0F);
        } else if (type == double.class) {
            field.set(null, 0D);
        } else if (type == JsonValue.class) {
            field.set(null, new JsonValue(JsonValue.ValueType.nullValue));
        } else {
            throw new IllegalStateException("Default value not available for " + type);
        }
    }

    /**
     * Parses the given JsonValue into the specified type taking into account any range restrictions.
     *
     * @param element the JsonValue to be parsed
     * @param type the target type to parse the id into
     * @param ranged specifies if there are range restrictions for numeric types
     * @return the parsed value of the id into the specified type
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object parseValue(JsonValue element, Class<?> type, @Nullable Ranged ranged) {
        if (JsonValue.class.isAssignableFrom(type)) {
            return element;
        } else if (type == String.class && element.isString()) {
            return element.asString();
        } else if (type == Number.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.isDouble() ? element.asDouble() : element.asLong(), ranged);
            return element.isDouble() ? element.asDouble() : element.asLong();
        } else if (type == Boolean.class && element.isBoolean()) {
            return element.asBoolean();
        } else if (type == UUID.class && element.isString()) {
            return UUID.fromString(element.asString());
        } else if (type == BigInteger.class && element.isNumber()) {
            return BigInteger.valueOf(element.asLong());
        } else if (type == BigDecimal.class && element.isNumber()) {
            return BigDecimal.valueOf(element.asDouble());
        } else if (type == Byte.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asByte(), ranged).byteValue();
            return element.asByte();
        } else if (type == Short.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asShort(), ranged).shortValue();
            return element.asShort();
        } else if (type == Integer.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asInt(), ranged).intValue();
            return element.asInt();
        } else if (type == Long.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asLong(), ranged).longValue();
            return element.asLong();
        } else if (type == Float.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asFloat(), ranged).floatValue();
            return element.asFloat();
        } else if (type == Double.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asDouble(), ranged).doubleValue();
            return element.asDouble();
        } else if (type == Character.class && element.isString()) {
            return element.asString().charAt(0);
        } else if (type == boolean.class && element.asBoolean()) {
            return element.asBoolean();
        } else if (type == char.class && element.isString()) {
            return element.asString().charAt(0);
        } else if (type == byte.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asByte(), ranged).byteValue();
            return element.asByte();
        } else if (type == short.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asShort(), ranged).shortValue();
            return element.asShort();
        } else if (type == int.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asInt(), ranged).intValue();
            return element.asInt();
        } else if (type == long.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asLong(), ranged).longValue();
            return element.asLong();
        } else if (type == float.class && element.isNumber()) {
            if (ranged != null) return parseNumber(element.asFloat(), ranged).floatValue();
            return element.asFloat();
        } else if (type == double.class && element.isDouble()) {
            if (ranged != null) return parseNumber(element.asDouble(), ranged).doubleValue();
            return element.asDouble();
        } else if (type == NamespaceID.class && element.isString()) {
            NamespaceID namespaceID = NamespaceID.tryParse(element.asString());
            if (namespaceID == null) throw new IllegalArgumentException("Invalid identifier: " + element.asString());
            return namespaceID;
        } else if (type == Enum.class && element.isString()) {
            return Enum.valueOf((Class<Enum>) type, element.asString());
        } else if (type == JsonValue.class) {
            return element;
        } else {
            throw new IllegalStateException("Unsupported type " + type);
        }
    }

    /**
     * Parses a number from a Json5Number object within the specified range.
     *
     * @param element the Json5Number object to parse
     * @param ranged the range within which the parsed number should fall
     * @return the parsed number within the specified range
     */
    private Number parseNumber(Number element, @Nullable Ranged ranged) {
        if (ranged == null) return element;
        if (ranged.min() > element.doubleValue()) return ranged.min();
        if (ranged.max() < element.doubleValue()) return ranged.max();
        return element;
    }

    /**
     * Retrieves a nested id in a JSON-like structure based on the provided path.
     *
     * @param root The root JsonValue where the search starts.
     * @param path The path to the desired id separated by dots.
     * @return The id found at the specified path, or null if not found.
     */
    private JsonValue getElement(JsonValue root, String path) {
        // Split the path into individual elements
        String[] pathElements = path.split("\\.");

        // Start traversal from the root
        JsonValue current = root;

        // Navigate through the structure until the second last id
        for (int i = 0; i < pathElements.length - 1; i++) {
            if (current instanceof JsonValue) {
                current = current.get(pathElements[i]);
                continue;
            }

            return null;
        }

        // Return the id at the last path id, if it exists
        return current instanceof JsonValue ? current.get(pathElements[pathElements.length - 1]) : null;
    }
    /**
     * Process the given field and perform various operations like setting accessibility,
     * updating maps, and setting default values.
     *
     * @param field the field to be processed
     */
    private void processEntry(Field field) {
        // Get the ConfigEntry annotation from the field
        ConfigEntry configEntry = field.getAnnotation(ConfigEntry.class);
        // If the field has the Ranged annotation, update rangesMap
        if (field.isAnnotationPresent(Ranged.class)) {
            Ranged ranged = field.getAnnotation(Ranged.class);
            // Validate the range values
            if (ranged.min() > ranged.max()) {
                throw new IllegalArgumentException("Ranged min cannot be greater than max");
            }
            this.rangesMap.put(configEntry.path(), ranged);
        }
        // Set the field to be accessible
        field.setAccessible(true);

        // Update entriesMap with the configEntry path and the configEntry itself
        entriesMap.put(configEntry.path(), configEntry);
        try {
            // If the field is defaulted or has a non-null value, update defaultsMap with the value
            if (configEntry.defaulted() || field.get(null) != null) {
                Object value = field.get(null);
                if (value != null) defaultsMap.put(configEntry.path(), value);
            } else {
                Class<?> type = field.getType();

                defineDefaults(field, type, configEntry);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        fieldsMap.put(configEntry.path(), field);
        typesMap.put(configEntry.path(), field.getType());
    }

    /**
     * Defines default values for different types of fields.
     *
     * @param  field        the field to define defaults for
     * @param  type         the type of the field
     * @param  configEntry  the configuration entry for the field
     */
    private void defineDefaults(Field field, Class<?> type, ConfigEntry configEntry) {
        if (type == JsonValue.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(JsonValue.ValueType.object));
        } else if (type == String.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(""));
        } else if (type == Number.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0));
        } else if (type == Boolean.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(false));
        } else if (type == Character.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(" "));
        } else if (type == Byte.class) {
            defaultsMap.put(configEntry.path(), new JsonValue((byte) 0));
        } else if (type == Short.class) {
            defaultsMap.put(configEntry.path(), new JsonValue((short) 0));
        } else if (type == Integer.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0));
        } else if (type == Long.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0L));
        } else if (type == Float.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0f));
        } else if (type == Double.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0d));
        } else if (type == boolean.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(false));
        } else if (type == char.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(" "));
        } else if (type == byte.class) {
            defaultsMap.put(configEntry.path(), new JsonValue((byte) 0));
        } else if (type == short.class) {
            defaultsMap.put(configEntry.path(), new JsonValue((short) 0));
        } else if (type == int.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0));
        } else if (type == long.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0L));
        } else if (type == float.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0f));
        } else if (type == double.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0d));
        } else if (type == BigInteger.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0L));
        } else if (type == BigDecimal.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(0.0));
        } else if (type == JsonValue.class) {
            defaultsMap.put(configEntry.path(), new JsonValue(JsonValue.ValueType.nullValue));
        } else if (type == UUID.class) {
            defaultsMap.put(configEntry.path(), new JsonValue("00000000-0000-0000-0000-000000000000"));
        } else if (type == Enum.class) {
            throw new IllegalStateException("Enums require a default value to be set (field " + field.getName() + ")");
        } else if (type == NamespaceID.class) {
            throw new IllegalStateException("Identifiers require a default value to be set (field " + field.getName() + ")");
        } else {
            throw new IllegalStateException("Unsupported default type " + type);
        }
    }

    /**
     * Loads the configuration file, handles exceptions, and then saves the configuration.
     */
    public void load() {
        try {
            this.loadUnsafe();
        } catch (NoSuchFileException ignored) {
            // File not found, can be ignored
        } catch (Exception e) {
            // Log error if failed to load the config file
            CommonConstants.LOGGER.error("Failed to load config file", e);
            return;
        }

        this.save();
    }

    /**
     * Retrieves the value of the field at the specified path.
     *
     * @param path The path of the field to retrieve.
     * @return The value of the field.
     * @throws IllegalArgumentException If the field does not exist.
     * @throws RuntimeException If there is an error accessing the field.
     */
    public Object get(String path) {
        // Get the field from the fieldsMap
        Field field = fieldsMap.get(path);

        // Check if the field exists
        if (field == null) {
            throw new IllegalArgumentException("Config entry " + path + " does not exist");
        }

        try {
            // Get the value of the field
            return field.get(this);
        } catch (IllegalAccessException e) {
            // Wrap the exception in a RuntimeException and rethrow it
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the class type of the field at the specified path.
     *
     * @param path the path of the field
     * @return the class type of the field
     * @throws IllegalArgumentException if the field does not exist
     */
    public Class<?> getType(String path) {
        // Get the field at the specified path
        Field field = fieldsMap.get(path);

        // If the field does not exist, throw an exception
        if (field == null) {
            throw new IllegalArgumentException("Config entry " + path + " does not exist");
        }

        // Return the class type of the field
        return field.getType();
    }

    /**
     * Retrieves the default value associated with the provided path.
     *
     * @param path The path to the config entry
     * @return The default value for the config entry
     * @throws IllegalArgumentException if the config entry does not exist
     */
    public Object getDefault(String path) {
        Field field = fieldsMap.get(path);
        if (field == null) {
            throw new IllegalArgumentException("Config entry " + path + " does not exist");
        }

        return defaultsMap.get(path);
    }

    /**
     * Checks if the given path exists in the fields map.
     *
     * @param path The path to check.
     * @return True if the path exists, false otherwise.
     */
    public boolean contains(String path) {
        return fieldsMap.containsKey(path);
    }

    /**
     * Retrieves all fields and their values from the object.
     *
     * @return A map containing field names as keys and their corresponding values
     */
    public Map<String, Object> getAll() {
        // Create a map to store field names and values
        Map<String, Object> map = new HashMap<>();

        // Iterate through all fields in the object
        for (Map.Entry<String, Field> entry : fieldsMap.entrySet()) {
            try {
                // Put the field name and its value into the map
                map.put(entry.getKey(), entry.getValue().get(this));
            } catch (IllegalAccessException e) {
                // Throw a runtime exception if access to the field is not allowed
                throw new RuntimeException(e);
            }
        }

        // Return the map containing all fields and their values
        return map;
    }

    /**
     * Returns an unmodifiable map of default values.
     *
     * @return unmodifiable map of default values
     */
    public Map<String, Object> getDefaults() {
        return Collections.unmodifiableMap(defaultsMap);
    }

    /**
     * Sets the value of a config entry specified by its path.
     *
     * @param path The path of the config entry.
     * @param value The value to set.
     * @throws IllegalArgumentException If the config entry does not exist or if the value cannot be set.
     */
    public void set(String path, Object value) {
        // Get the field associated with the given path
        Field field = fieldsMap.get(path);

        // If the field does not exist, throw an exception
        if (field == null) {
            throw new IllegalArgumentException("Config entry " + path + " does not exist");
        }

        try {
            // Set the value of the field
            field.set(this, value);
        } catch (IllegalAccessException e) {
            // If an IllegalAccessException is caught, rethrow it as a RuntimeException
            throw new RuntimeException(e);
        } catch (Exception e) {
            // If any other exception is caught, throw an IllegalArgumentException with a specific error message
            throw new IllegalArgumentException("Failed to set config entry " + path, e);
        } finally {
            // Save the config after setting the value
            this.save();
        }
    }

    /**
     * Resets the config entry based on the provided path.
     *
     * @param path The path of the config entry to reset
     */
    public void reset(String path) {
        // Get the field from the fields map based on the path
        Field field = fieldsMap.get(path);

        // If the field is not found, throw an IllegalArgumentException
        if (field == null) {
            throw new IllegalArgumentException("Config entry " + path + " does not exist");
        }

        try {
            // Set the defaults for the field
            this.setDefaults(field, field.getType());
        } catch (IllegalAccessException e) {
            // If there's an IllegalAccessException, throw a RuntimeException
            throw new RuntimeException(e);
        } catch (Exception e) {
            // If there's any other exception, throw an IllegalArgumentException with the original cause
            throw new IllegalArgumentException("Failed to reset config entry " + path, e);
        }
    }

    public String getFileName() {
        return configPath.name();
    }

    public Mod getMod() {
        return mod;
    }

    public FileHandle getConfigPath() {
        return configPath;
    }

    public Ranged getRange(String key) {
        return rangesMap.get(key);
    }

    public String getComment(String key) {
        return entriesMap.get(key).comment();
    }

    /**
     * Event that is called when the config file is loaded or reloaded.
     *
     * @see #event
     */
    @FunctionalInterface
    public interface LoadConfig {
        /**
         * Called when the config file is loaded or reloaded.
         *
         * @throws IOException if an I/O error occurs
         */
        void load() throws IOException;
    }
}