package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import de.damios.guacamole.Preconditions;
import dev.ultreon.libs.collections.v0.tables.HashTable;
import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.render.meshing.FaceCull;
import dev.ultreon.quantum.client.render.meshing.Light;
import dev.ultreon.quantum.client.world.AOUtils;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Axis;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class Json5ModelLoader {
    private final ResourceManager resourceManager;

    /**
     * Constructs a new Json5ModelLoader instance, initializing it with the specified ResourceManager.
     *
     * @param resourceManager the resource manager to be used for loading resources
     */
    public Json5ModelLoader(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Default constructor for the Json5ModelLoader class.
     * Initializes the Json5ModelLoader using the default resource manager obtained
     * from the QuantumClient.
     */
    public Json5ModelLoader() {
        this(QuantumClient.get().getResourceManager());
    }

    /**
     * Loads a {@link Json5Model} associated with the given {@link Block}.
     * This method attempts to resolve the resource path for the block's JSON5 model file,
     * retrieves the resource, and parses it into a {@link Json5Model}.
     * If the resource is not found, it returns null.
     *
     * @param block the {@link Block} whose model is to be loaded
     * @return the loaded {@link Json5Model}, or null if the resource cannot be found
     * @throws IOException if an I/O error occurs during reading of the resource
     */
    public Json5Model load(Block block) throws IOException {
        NamespaceID namespaceID = block.getId().mapPath(path -> "models/blocks/" + path + ".json5");
        Resource resource = this.resourceManager.getResource(namespaceID);
        if (resource == null)
            return null;
        QuantumClient.LOGGER.debug("Loading block model: {}", namespaceID);
        return this.load(Registries.BLOCK.getKey(block), JSON_READ.parse(resource.openReader()));
    }

    /**
     * Loads a {@link Json5Model} associated with the specified {@link Item}.
     * This method attempts to resolve the resource path for the item's JSON5 model file,
     * retrieves the resource, and parses it into a {@link Json5Model}. If the resource
     * cannot be found, this method returns null.
     *
     * @param item the {@link Item} whose model is to be loaded
     * @return the loaded {@link Json5Model}, or null if the resource cannot be found
     * @throws IOException if an I/O error occurs during reading of the resource
     */
    public Json5Model load(Item item) throws IOException {
        NamespaceID namespaceID = item.getId().mapPath(path -> "models/items/" + path + ".json5");
        Resource resource = this.resourceManager.getResource(namespaceID);
        if (resource == null)
            return null;
        QuantumClient.LOGGER.debug("Loading item model: {}", namespaceID);
        return this.load(Registries.ITEM.getKey(item), JSON_READ.parse(resource.openReader()));
    }

    /**
     * Loads a {@link Json5Model} based on the provided registry key and model data.
     * Validates the registry key to ensure it belongs to either blocks or items,
     * parses the textures, elements, display properties, and other model-related
     * configurations from the given JSON5 element, and constructs the model.
     *
     * @param key       the {@link RegistryKey} for which the model needs to be loaded;
     *                  must belong to either {@code RegistryKeys.BLOCK} or {@code RegistryKeys.ITEM}
     * @param modelData the JSON5 representation of the model data
     * @return the constructed {@link Json5Model} containing all the parsed and processed model data
     * @throws IllegalArgumentException if the provided registry key does not belong to blocks or items
     */
    @SuppressWarnings("SpellCheckingInspection")
    public Json5Model load(RegistryKey<?> key, JsonValue modelData) {
        if (!Objects.equals(key.parent(), RegistryKeys.BLOCK) && !Objects.equals(key.parent(), RegistryKeys.ITEM)) {
            throw new IllegalArgumentException("Invalid model key, must be block or item: " + key);
        }

        JsonValue root = modelData;
        JsonValue textures = root.get("textures");
        Map<String, NamespaceID> textureElements = loadTextures(textures);

//        GridPoint2 textureSize = loadVec2i(root.get("texture_size"), new GridPoint2(16, 16));
        GridPoint2 textureSize = new GridPoint2(16, 16);

        JsonValue elements = root.get("elements");
        List<ModelElement> modelElements = loadElements(elements, textureSize.x, textureSize.y);

        JsonValue ambientocclusion = root.get("ambientocclusion");
        boolean ambientOcclusion = ambientocclusion == null || ambientocclusion.asBoolean();

        Table<String, BlockDataEntry<?>, Json5Model> overrides = null;

        JsonValue displayJson = root.get("display");
        if (displayJson == null)
            displayJson = new JsonValue(JsonValue.ValueType.object);

        // TODO: Allow display properties.
        Display display = Display.read(displayJson);

        return new Json5Model(key.id(), textureElements, modelElements, ambientOcclusion, display, overrides);
    }

    private Table<String, BlockDataEntry<?>, Json5Model> loadOverrides(RegistryKey<Block> key, JsonValue overridesJson5) {
        Table<String, BlockDataEntry<?>, Json5Model> overrides = new HashTable<>();
        Block block = Registries.BLOCK.get(key);
        BlockState meta = block.getDefaultState();

        for (JsonValue overrideElem : overridesJson5) {
            String keyName = overrideElem.name;
            JsonValue overrideObj = overrideElem;

            Json5Model model = load(key, overrideObj);
            BlockDataEntry<?> entry1 = meta.get(keyName);

            if (model == null)
                throw new IllegalArgumentException("Invalid model override: " + keyName);

            overrides.put(keyName, entry1.parse(overrideObj), model);
        }

        return overrides;
    }

    private List<ModelElement> loadElements(JsonValue elements, int textureWidth, int textureHeight) {
        List<ModelElement> modelElements = new ArrayList<>();

        for (JsonValue elem : elements) {
            JsonValue element = elem;
            JsonValue faces = element.get("faces");
            Map<Direction, FaceElement> blockFaceFaceElementMap = loadFaces(faces, textureWidth, textureHeight);

            JsonValue shade1 = element.get("shade");
            boolean shade = shade1 != null && shade1.asBoolean();
            JsonValue rotation1 = element.get("rotation");
            ElementRotation rotation = ElementRotation.deserialize(rotation1 == null ? null : rotation1);

            Vector3 from = loadVec3(element.get("from"));
            Vector3 to = loadVec3(element.get("to"));

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
        Resource resource = this.resourceManager.getResource(id.mapPath(path -> "models/" + path + ".json5"));
        if (resource != null) {
            return this.load(key, resource.loadJson());
        }

        return null;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static final class FaceElement {
        private final String texture;
        private final UVs uvs;
        private final int rotation;
        private final int tintindex;
        private final String cullface;

        public FaceElement(String texture, UVs uvs, int rotation, int tintindex,
                           String cullface) {
            this.texture = texture;
            this.uvs = uvs;
            this.rotation = rotation;
            this.tintindex = tintindex;
            this.cullface = cullface;
        }

        public String texture() {
            return texture;
        }

        public UVs uvs() {
            return uvs;
        }

        public int rotation() {
            return rotation;
        }

        public int tintindex() {
            return tintindex;
        }

        public String cullface() {
            return cullface;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (FaceElement) obj;
            return Objects.equals(this.texture, that.texture) &&
                    Objects.equals(this.uvs, that.uvs) &&
                    this.rotation == that.rotation &&
                    this.tintindex == that.tintindex &&
                    Objects.equals(this.cullface, that.cullface);
        }

        @Override
        public int hashCode() {
            return Objects.hash(texture, uvs, rotation, tintindex, cullface);
        }

        @Override
        public String toString() {
            return "FaceElement[" +
                    "texture=" + texture + ", " +
                    "uvs=" + uvs + ", " +
                    "rotation=" + rotation + ", " +
                    "tintindex=" + tintindex + ", " +
                    "cullface=" + cullface + ']';
        }

    }

    public static final class UVs {
        private final float x1;
        private final float y1;
        private final float x2;
        private final float y2;

        public UVs(float x1, float y1, float x2, float y2) {
            this.x1 = x1 / 16.0F;
            this.y1 = y1 / 16.0F;
            this.x2 = x2 / 16.0F;
            this.y2 = y2 / 16.0F;
        }

        public UVs(float x1, float y1, float x2, float y2, int textureWidth, int textureHeight) {
            this.x1 = x1 / textureWidth;
            this.y1 = y1 / textureHeight;
            this.x2 = x2 / textureWidth;
            this.y2 = y2 / textureHeight;
        }

        public float x1() {
            return x1;
        }

        public float y1() {
            return y1;
        }

        public float x2() {
            return x2;
        }

        public float y2() {
            return y2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (UVs) obj;
            return Float.floatToIntBits(this.x1) == Float.floatToIntBits(that.x1) &&
                    Float.floatToIntBits(this.y1) == Float.floatToIntBits(that.y1) &&
                    Float.floatToIntBits(this.x2) == Float.floatToIntBits(that.x2) &&
                    Float.floatToIntBits(this.y2) == Float.floatToIntBits(that.y2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x1, y1, x2, y2);
        }

        @Override
        public String toString() {
            return "UVs[" +
                    "x1=" + x1 + ", " +
                    "y1=" + y1 + ", " +
                    "x2=" + x2 + ", " +
                    "y2=" + y2 + ']';
        }


    }

    public static final class ModelElement {
        private static final Vector3 tmp = new Vector3();
        private static final Quaternion tmpQ = new Quaternion();
        private final Map<Direction, FaceElement> blockFaceFaceElementMap;
        private final boolean shade;
        private final ElementRotation rotation;
        private final Vector3 from;
        private final Vector3 to;
        private final Vector3 tmp2 = new Vector3();


        public ModelElement(Map<Direction, FaceElement> blockFaceFaceElementMap, boolean shade, ElementRotation rotation, Vector3 from, Vector3 to) {
            Preconditions.checkNotNull(blockFaceFaceElementMap);
            Preconditions.checkNotNull(rotation);
            Preconditions.checkNotNull(from);
            Preconditions.checkNotNull(to);
            this.blockFaceFaceElementMap = blockFaceFaceElementMap;
            this.shade = shade;
            this.rotation = rotation;
            this.from = from;
            this.to = to;
        }

        public void bake(int idx, MeshPartBuilder meshBuilder, Map<String, NamespaceID> textureElements, int x, int y, int z, int cull, int[] ao, long light) {
            final var from = this.from();
            final var to = this.to();

            final var v00 = new VertexInfo();
            final var v01 = new VertexInfo();
            final var v10 = new VertexInfo();
            final var v11 = new VertexInfo();
            for (var $ : blockFaceFaceElementMap.entrySet()) {
                final var direction = $.getKey();
                if (FaceCull.culls(direction, cull)) continue;
                final var faceElement = $.getValue();
                final var texRef = faceElement.texture;
                final @Nullable NamespaceID texture = Objects.equals(texRef, "#missing") ? NamespaceID.of("blocks/error")
                        : texRef.startsWith("#") ? textureElements.get(texRef.substring(1))
                        : NamespaceID.parse(texRef).mapPath(path -> path);


                int sAo = AOUtils.aoForSide(ao, direction);
                byte sLight = Light.get(light, direction);
                v00.setCol(0f, AOUtils.hasAoCorner00(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);
                v01.setCol(0f, AOUtils.hasAoCorner01(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);
                v10.setCol(0f, AOUtils.hasAoCorner10(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);
                v11.setCol(0f, AOUtils.hasAoCorner11(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);

                v00.setNor(direction.getNormal());
                v01.setNor(direction.getNormal());
                v10.setNor(direction.getNormal());
                v11.setNor(direction.getNormal());

                var region = QuantumClient.get().blocksTextureAtlas.get(Objects.requireNonNull(texture), TextureAtlas.TextureAtlasType.DIFFUSE);
                if (region == null) {
                    region = QuantumClient.get().blocksTextureAtlas.get(NamespaceID.of("blocks/error"), TextureAtlas.TextureAtlasType.DIFFUSE);

                    if (region == null) throw new IllegalArgumentException("Undefined error texture! " + texture);

                    v00.setUV(region.getU(), region.getV2());
                    v01.setUV(region.getU(), region.getV());
                    v10.setUV(region.getU2(), region.getV2());
                    v11.setUV(region.getU2(), region.getV());
                } else {
                    float u0 = region.getU() + (faceElement.uvs.x1) * (region.getU2() - region.getU());
                    float v0 = region.getV() + (faceElement.uvs.y2) * (region.getV2() - region.getV());

                    float u1 = region.getU() + (faceElement.uvs.x1) * (region.getU2() - region.getU());
                    float v1 = region.getV() + (faceElement.uvs.y1) * (region.getV2() - region.getV());

                    float u2 = region.getU() + (faceElement.uvs.x2) * (region.getU2() - region.getU());
                    float v2 = region.getV() + (faceElement.uvs.y2) * (region.getV2() - region.getV());

                    float u3 = region.getU() + (faceElement.uvs.x2) * (region.getU2() - region.getU());
                    float v3 = region.getV() + (faceElement.uvs.y1) * (region.getV2() - region.getV());

                    v00.setUV(u0, v0);
                    v01.setUV(u1, v1);
                    v10.setUV(u2, v2);
                    v11.setUV(u3, v3);
                }

                switch (direction) {
                    case UP:
                        v01.setPos(from.x / 16, to.y / 16, from.z / 16);
                        v00.setPos(from.x / 16, to.y / 16, to.z / 16);
                        v11.setPos(to.x / 16, to.y / 16, from.z / 16);
                        v10.setPos(to.x / 16, to.y / 16, to.z / 16);
                        break;
                    case DOWN:
                        v00.setPos(from.x / 16, from.y / 16, from.z / 16);
                        v01.setPos(from.x / 16, from.y / 16, to.z / 16);
                        v10.setPos(to.x / 16, from.y / 16, from.z / 16);
                        v11.setPos(to.x / 16, from.y / 16, to.z / 16);
                        break;
                    case WEST:
                        v00.setPos(from.x / 16, from.y / 16, from.z / 16);
                        v01.setPos(from.x / 16, to.y / 16, from.z / 16);
                        v10.setPos(from.x / 16, from.y / 16, to.z / 16);
                        v11.setPos(from.x / 16, to.y / 16, to.z / 16);
                        break;
                    case EAST:
                        v10.setPos(to.x / 16, from.y / 16, from.z / 16);
                        v11.setPos(to.x / 16, to.y / 16, from.z / 16);
                        v00.setPos(to.x / 16, from.y / 16, to.z / 16);
                        v01.setPos(to.x / 16, to.y / 16, to.z / 16);
                        break;
                    case NORTH:
                        v00.setPos(to.x / 16, from.y / 16, from.z / 16);
                        v01.setPos(to.x / 16, to.y / 16, from.z / 16);
                        v10.setPos(from.x / 16, from.y / 16, from.z / 16);
                        v11.setPos(from.x / 16, to.y / 16, from.z / 16);
                        break;
                    case SOUTH:
                        v10.setPos(to.x / 16, from.y / 16, to.z / 16);
                        v11.setPos(to.x / 16, to.y / 16, to.z / 16);
                        v00.setPos(from.x / 16, from.y / 16, to.z / 16);
                        v01.setPos(from.x / 16, to.y / 16, to.z / 16);
                        break;
                }

                rotate(v00, v01, v10, v11, rotation);

                v00.position.add(x, y, z);
                v01.position.add(x, y, z);
                v10.position.add(x, y, z);
                v11.position.add(x, y, z);

                meshBuilder.rect(v00, v10, v11, v01);

                final var material = new Material();
                material.set(TextureAttribute.createDiffuse(QuantumClient.get().blocksTextureAtlas.getTexture()));
                material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                material.set(new FloatAttribute(FloatAttribute.AlphaTest));
                material.set(new DepthTestAttribute(GL20.GL_LEQUAL));
            }
        }

        private void rotate(
                VertexInfo v00,
                VertexInfo v01,
                VertexInfo v10,
                VertexInfo v11,
                ElementRotation rotation
        ) {
            final var originVec = rotation.originVec;
            final var axis = rotation.axis;
            final var angle = rotation.angle;
            final var rescale = rotation.rescale; // TODO: implement

            // Rotate the vertices
            rotate(v00.position, originVec, axis, angle, v00.position);
            rotate(v01.position, originVec, axis, angle, v01.position);
            rotate(v10.position, originVec, axis, angle, v10.position);
            rotate(v11.position, originVec, axis, angle, v11.position);

        }

        public Vector3 rotate(Vector3 position, Vector3 originVec, Axis axis, float degrees, Vector3 out) {
            return rotateVector(position, originVec, degrees, axis.getVector(), out);
        }

        // Reusable instances
        private final Vector3 tmp1 = new Vector3();

        private final Matrix4 rotationMatrix = new Matrix4();

        private Vector3 rotateVector(
                Vector3 vector,
                Vector3 origin,
                float angleDegrees,
                Vector3 axis,
                Vector3 result
        ) {
            // Step 1: Translate the vector to the origin
            tmp1.set(vector).sub(tmp2.set(origin).scl(1 / 16f));

            // Step 2: Create a rotation matrix
            rotationMatrix.setToRotation(axis, angleDegrees);

            // Step 3: Apply the rotation matrix to the translated vector
            tmp1.mul(rotationMatrix);

            // Step 4: Translate the vector back
            result.set(tmp1).add(tmp2);

            return result;
        }

        public Map<Direction, FaceElement> blockFaceFaceElementMap() {
            return blockFaceFaceElementMap;
        }

        public boolean shade() {
            return shade;
        }

        public ElementRotation rotation() {
            return rotation;
        }

        public Vector3 from() {
            return from;
        }

        public Vector3 to() {
            return to;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ModelElement) obj;
            return Objects.equals(this.blockFaceFaceElementMap, that.blockFaceFaceElementMap) &&
                    this.shade == that.shade &&
                    Objects.equals(this.rotation, that.rotation) &&
                    Objects.equals(this.from, that.from) &&
                    Objects.equals(this.to, that.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blockFaceFaceElementMap, shade, rotation, from, to);
        }

        @Override
        public String toString() {
            return "ModelElement[" +
                    "blockFaceFaceElementMap=" + blockFaceFaceElementMap + ", " +
                    "shade=" + shade + ", " +
                    "rotation=" + rotation + ", " +
                    "from=" + from + ", " +
                    "to=" + to + ']';
        }

    }

    /**
     * Represents a rotational transformation applied to an element, defined by its origin, axis,
     * angle of rotation, and an optional rescaling flag.
     * <p>
     * This class is used to manage rotation information for elements in a 3D space, where the
     * rotation is specified with respect to a defined origin vector and a chosen axis (X, Y, or Z).
     * The amount of rotation is determined by an angle measured in degrees, and optionally, the
     * transformation can apply rescaling to maintain proportionality.
     * <p>
     * Instances of this class are immutable and provide methods to retrieve the properties of the rotation.
     */
    public static final class ElementRotation {
        public static final ElementRotation ZERO = new ElementRotation(Vector3.Zero, Axis.X, 0f, false);
        private final Vector3 originVec;
        private final Axis axis;
        private final float angle;
        private final boolean rescale;

        /**
         * @param originVec the origin vector representing the point around which the element is rotated
         * @param axis      the axis of rotation (X, Y, or Z)
         * @param angle     the angle of rotation in degrees
         * @param rescale   whether to apply rescaling after the rotation
         */
        public ElementRotation(Vector3 originVec, Axis axis, float angle, boolean rescale) {
            this.originVec = originVec;
            this.axis = axis;
            this.angle = angle;
            this.rescale = rescale;
        }

        public static ElementRotation deserialize(@Nullable JsonValue rotation) {
            if (rotation == null) {
                return new ElementRotation(new Vector3(0, 0, 0), Axis.Y, 0, false);
            }

            float[] origin = rotation.get("origin").asFloatArray();
            String axis = rotation.get("axis").asString();
            float angle = rotation.get("angle").asFloat();
            JsonValue rescale1 = rotation.get("rescale");
            boolean rescale = rescale1 == null || rescale1.asBoolean();

            Vector3 originVec = new Vector3(origin[0], origin[1], origin[2]);
            return new ElementRotation(originVec, Axis.valueOf(axis.toUpperCase(Locale.ROOT)), angle, rescale);
        }

        @Override
        public @NotNull String toString() {
            return "ElementRotation[" +
                    "originVec=" + originVec + ", " +
                    "axis=" + axis + ", " +
                    "angle=" + angle + ", " +
                    "rescale=" + rescale + ']';
        }

        public Vector3 originVec() {
            return originVec;
        }

        public Axis axis() {
            return axis;
        }

        public float angle() {
            return angle;
        }

        public boolean rescale() {
            return rescale;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ElementRotation) obj;
            return Objects.equals(this.originVec, that.originVec) &&
                    Objects.equals(this.axis, that.axis) &&
                    Float.floatToIntBits(this.angle) == Float.floatToIntBits(that.angle) &&
                    this.rescale == that.rescale;
        }

        @Override
        public int hashCode() {
            return Objects.hash(originVec, axis, angle, rescale);
        }


    }

    public static final class Display {
        public String renderPass;

        public Display(String renderPass) {
            this.renderPass = renderPass;
        }

        public static Display read(JsonValue display) {
            JsonValue renderPassJson = display.get("renderPass");
            String renderPass = renderPassJson != null ? renderPassJson.asString() : "opaque";
            return new Display(renderPass);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return "Display[]";
        }


    }
}
