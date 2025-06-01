package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.libs.collections.v0.tables.HashTable;
import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.property.BlockDataEntry;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.item.BlockItem;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Direction;

import java.io.IOException;
import java.util.*;

import static dev.ultreon.quantum.CommonConstants.JSON_READ;

/**
 * The Json5ModelLoader class is responsible for loading and processing JSON5 formatted models
 * for blocks and items in a resource management context. This class provides methods to load
 * models and their associated data such as textures, elements, and overrides.
 * <p>
 * The class supports both block and item models, providing utility methods to map their attributes
 * from JSON5 files to internal data structures for rendering purposes.
 * <p>
 * It uses a ResourceManager for accessing model resources and registry keys to identify them.
 */
public class JsonModelLoader {
    private final ResourceManager resourceManager;

    /**
     * Constructs a new Json5ModelLoader instance, initializing it with the specified ResourceManager.
     *
     * @param resourceManager the resource manager to be used for loading resources
     */
    public JsonModelLoader(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Default constructor for the Json5ModelLoader class.
     * Initializes the Json5ModelLoader using the default resource manager obtained
     * from the QuantumClient.
     */
    public JsonModelLoader() {
        this(QuantumClient.get().getResourceManager());
    }

    /**
     * Loads a {@link JsonModel} associated with the given {@link Block}.
     * This method attempts to resolve the resource path for the block's JSON5 model file,
     * retrieves the resource, and parses it into a {@link JsonModel}.
     * If the resource is not found, it returns null.
     *
     * @param block the {@link Block} whose model is to be loaded
     * @return the loaded {@link JsonModel}, or null if the resource cannot be found
     * @throws IOException if an I/O error occurs during reading of the resource
     */
    public JsonModel load(Block block) throws IOException {
        NamespaceID namespaceID = block.getId().mapPath(path -> "models/blocks/" + path + ".quant");
        Resource resource = this.resourceManager.getResource(namespaceID);
        if (resource == null)
            return null;
        QuantumClient.LOGGER.debug("Loading block model: {}", namespaceID);
        JsonModel model = this.load(Registries.BLOCK.getKey(block), JSON_READ.parse(resource.openReader()));
        model.setBlock(block.getDefaultState());
        return model;
    }

    /**
     * Loads a {@link JsonModel} associated with the specified {@link Item}.
     * This method attempts to resolve the resource path for the item's JSON5 model file,
     * retrieves the resource, and parses it into a {@link JsonModel}. If the resource
     * cannot be found, this method returns null.
     *
     * @param item the {@link Item} whose model is to be loaded
     * @return the loaded {@link JsonModel}, or null if the resource cannot be found
     * @throws IOException if an I/O error occurs during reading of the resource
     */
    public JsonModel load(Item item) throws IOException {
        NamespaceID namespaceID = item.getId().mapPath(path -> "models/items/" + path + ".quant");
        Resource resource = this.resourceManager.getResource(namespaceID);
        if (resource == null)
            return null;
        QuantumClient.LOGGER.debug("Loading item model: {}", namespaceID);
        JsonModel model = this.load(Registries.ITEM.getKey(item), JSON_READ.parse(resource.openReader()));
        if (item instanceof BlockItem) {
            model.setBlock(((BlockItem) item).getBlock().getDefaultState());
        }
        return model;
    }

    /**
     * Loads a {@link JsonModel} based on the provided registry key and model data.
     * Validates the registry key to ensure it belongs to either blocks or items,
     * parses the textures, elements, display properties, and other model-related
     * configurations from the given JSON5 element, and constructs the model.
     *
     * @param key       the {@link RegistryKey} for which the model needs to be loaded;
     *                  must belong to either {@code RegistryKeys.BLOCK} or {@code RegistryKeys.ITEM}
     * @param modelData the JSON5 representation of the model data
     * @return the constructed {@link JsonModel} containing all the parsed and processed model data
     * @throws IllegalArgumentException if the provided registry key does not belong to blocks or items
     */
    @SuppressWarnings("SpellCheckingInspection")
    public JsonModel load(RegistryKey<?> key, JsonValue modelData) {
        if (!Objects.equals(key.parent(), RegistryKeys.BLOCK) && !Objects.equals(key.parent(), RegistryKeys.ITEM)) {
            throw new IllegalArgumentException("Invalid model key, must be block or item: " + key);
        }

        JsonValue textures = modelData.get("textures");
        Map<String, NamespaceID> textureElements = loadTextures(textures);

//        GridPoint2 textureSize = loadVec2i(root.get("texture_size"), new GridPoint2(16, 16));
        GridPoint2 textureSize = new GridPoint2(16, 16);

        JsonValue elements = modelData.get("elements");
        List<ModelElement> modelElements = loadElements(elements, textureSize.x, textureSize.y);

        JsonValue ambientocclusion = modelData.get("ambientocclusion");
        boolean ambientOcclusion = ambientocclusion == null || ambientocclusion.asBoolean();

        Table<String, BlockDataEntry<?>, JsonModel> overrides = null;

        JsonValue displayJson = modelData.get("display");
        if (displayJson == null)
            displayJson = new JsonValue(JsonValue.ValueType.object);

        // TODO: Allow display properties.
        Display display = Display.read(displayJson);

        return new JsonModel(key.id(), textureElements, modelElements, ambientOcclusion, display, overrides);
    }

    private Table<String, Object, JsonModel> loadOverrides(RegistryKey<Block> key, JsonValue overridesJson5) {
        Table<String, Object, JsonModel> overrides = new HashTable<>();
        Block block = Registries.BLOCK.get(key);
        BlockState meta = block.getDefaultState();

        for (JsonValue overrideElem : overridesJson5) {
            String keyName = overrideElem.name;

            JsonModel model = load(key, overrideElem);
            Object entry1 = meta.get(block.getDefinition().keyByName(keyName));

            if (model == null)
                throw new IllegalArgumentException("Invalid model override: " + keyName);

            overrides.put(keyName, entry1, model);
        }

        return overrides;
    }

    private List<ModelElement> loadElements(JsonValue elements, int textureWidth, int textureHeight) {
        List<ModelElement> modelElements = new ArrayList<>();

        for (JsonValue elem : elements) {
            JsonValue faces = elem.get("faces");
            Map<Direction, FaceElement> blockFaceFaceElementMap = loadFaces(faces, textureWidth, textureHeight);

            JsonValue shade1 = elem.get("shade");
            boolean shade = shade1 != null && shade1.asBoolean();
            JsonValue rotation1 = elem.get("rotation");
            ElementRotation rotation = ElementRotation.deserialize(rotation1 == null ? null : rotation1);

            Vector3 from = loadVec3(elem.get("from"));
            Vector3 to = loadVec3(elem.get("to"));

            ModelElement modelElement = new ModelElement(blockFaceFaceElementMap, shade, rotation, from, to);
            modelElements.add(modelElement);
        }

        return modelElements;
    }

    private Vector3 loadVec3(JsonValue from) {
        float[] floatArray = from.asFloatArray();
        return new Vector3(floatArray[0], floatArray[1], floatArray[2]);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private Map<Direction, FaceElement> loadFaces(JsonValue faces, int textureWidth, int textureHeight) {
        Map<Direction, FaceElement> faceElems = new HashMap<>();
        for (JsonValue faceData : faces) {
            Direction direction = Direction.valueOf(faceData.name.toUpperCase(Locale.ROOT));
            JsonValue uvs = faceData.get("uv");
            String texture = faceData.get("texture").asString();
            JsonValue rotation1 = faceData.get("rotation");
            int rotation = rotation1 == null ? 0 : rotation1.asInt();
            JsonValue tintIndex1 = faceData.get("tintindex");
            int tintIndex = tintIndex1 == null ? -1 : tintIndex1.asInt();
            JsonValue cullface = faceData.get("cullface");
            String cullFace = cullface == null ? null : cullface.asString();

            faceElems.put(direction, new FaceElement(texture, new UVs(uvs.get(0).asInt(), uvs.get(1).asInt(), uvs.get(2).asInt(), uvs.get(3).asInt(), textureWidth, textureHeight), rotation, tintIndex, cullFace));
        }

        return faceElems;
    }

    private Map<String, NamespaceID> loadTextures(JsonValue textures) {
        Map<String, NamespaceID> textureElements = new HashMap<>();

        for (var entry : textures) {
            String name = entry.name;
            String stringId = entry.asString();
            NamespaceID id = NamespaceID.parse(stringId);
            textureElements.put(name, id);
        }

        return textureElements;
    }

    public BlockModel load(RegistryKey<?> key, NamespaceID id) {
        Resource resource = this.resourceManager.getResource(id.mapPath(path -> "models/" + path + ".quant"));
        if (resource != null) {
            return this.load(key, resource.loadJson());
        }

        return null;
    }
}
