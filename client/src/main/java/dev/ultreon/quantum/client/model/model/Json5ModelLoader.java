package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockDataEntry;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Axis;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.CubicDirection;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

import static dev.ultreon.quantum.CommonConstants.JSON5;

public class Json5ModelLoader {
    private final ResourceManager resourceManager;
    private RegistryKey<?> key;

    public Json5ModelLoader(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public Json5ModelLoader() {
        this(QuantumClient.get().getResourceManager());
    }

    public Json5Model load(Block block) throws IOException {
        NamespaceID namespaceID = block.getId().mapPath(path -> "models/blocks/" + path + ".json5");
        Resource resource = this.resourceManager.getResource(namespaceID);
        if (resource == null)
            return null;
        QuantumClient.LOGGER.debug("Loading block model: {}", namespaceID);
        return this.load(Registries.BLOCK.getKey(block), JSON5.parse(resource.openReader()));
    }

    public Json5Model load(Item item) throws IOException {
        NamespaceID namespaceID = item.getId().mapPath(path -> "models/items/" + path + ".json5");
        Resource resource = this.resourceManager.getResource(namespaceID);
        if (resource == null)
            return null;
        QuantumClient.LOGGER.debug("Loading item model: {}", namespaceID);
        return this.load(Registries.ITEM.getKey(item), JSON5.parse(resource.openReader()));
    }

    @SuppressWarnings("SpellCheckingInspection")
    public Json5Model load(RegistryKey<?> key, Json5Element modelData) {
        if (!key.parent().equals(RegistryKeys.BLOCK) && !key.parent().equals(RegistryKeys.ITEM)) {
            throw new IllegalArgumentException("Invalid model key, must be block or item: " + key);
        }

        Json5Object root = modelData.getAsJson5Object();
        Json5Object textures = root.getAsJson5Object("textures");
        Map<String, NamespaceID> textureElements = loadTextures(textures);

//        GridPoint2 textureSize = loadVec2i(root.getAsJson5Array("texture_size"), new GridPoint2(16, 16));
        GridPoint2 textureSize = new GridPoint2(16, 16);

        Json5Array elements = root.getAsJson5Array("elements");
        List<ModelElement> modelElements = loadElements(elements, textureSize.x, textureSize.y);

        Json5Element ambientocclusion = root.get("ambientocclusion");
        boolean ambientOcclusion = ambientocclusion == null || ambientocclusion.getAsBoolean();

        Table<String, BlockDataEntry<?>, Json5Model> overrides = null;
        if (key.parent().equals(RegistryKeys.BLOCK)) {
            Json5Object overridesJson5 = root.getAsJson5Object("overrides");
            if (overridesJson5 == null) overridesJson5 = new Json5Object();
            //noinspection unchecked
            overrides = loadOverrides((RegistryKey<Block>) key, overridesJson5);
        }

        // TODO: Allow display properties.
        Display display = new Display();

        return new Json5Model(key, textureElements, modelElements, ambientOcclusion, display, overrides);
    }

    private Table<String, BlockDataEntry<?>, Json5Model> loadOverrides(RegistryKey<Block> key, Json5Object overridesJson5) {
        Table<String, BlockDataEntry<?>, Json5Model> overrides = HashBasedTable.create();
        Block block = Registries.BLOCK.get(key);
        BlockProperties meta = block.createMeta();
        for (Map.Entry<String, Json5Element> entry : overridesJson5.entrySet()) {
            String keyName = entry.getKey();
            Json5Element overrideElem = entry.getValue();
            Json5Object overrideObj = overrideElem.getAsJson5Object();

            Json5Model model = load(key, overrideObj);
            BlockDataEntry<?> entry1 = meta.getProperty(keyName);
            if (entry1 == null)
                throw new IllegalArgumentException("Invalid model override: " + keyName);

            if (model == null)
                throw new IllegalArgumentException("Invalid model override: " + keyName);

            overrides.put(keyName, entry1.parse(overrideObj), model);
        }

        return overrides;
    }

    private GridPoint2 loadVec2i(Json5Array textureSize, GridPoint2 defaultValue) {
        if (textureSize == null)
            return defaultValue;
        if (textureSize.size() != 2)
            throw new IllegalArgumentException("Invalid 'texture_size' array: " + textureSize);
        return new GridPoint2(textureSize.get(0).getAsInt(), textureSize.get(1).getAsInt());
    }

    private List<ModelElement> loadElements(Json5Array elements, int textureWidth, int textureHeight) {
        List<ModelElement> modelElements = new ArrayList<>();

        for (Json5Element elem : elements) {
            Json5Object element = elem.getAsJson5Object();
            Json5Object faces = element.getAsJson5Object("faces");
            Map<CubicDirection, FaceElement> blockFaceFaceElementMap = loadFaces(faces, textureWidth, textureHeight);

            Json5Element shade1 = element.get("shade");
            boolean shade = shade1 != null && shade1.getAsBoolean();
            Json5Element rotation1 = element.get("rotation");
            ElementRotation rotation = ElementRotation.deserialize(rotation1 == null ? null : rotation1.getAsJson5Object());

            Vector3 from = loadVec3(element.getAsJson5Array("from"));
            Vector3 to = loadVec3(element.getAsJson5Array("to"));

            ModelElement modelElement = new ModelElement(blockFaceFaceElementMap, shade, rotation, from, to);
            modelElements.add(modelElement);
        }

        return modelElements;
    }

    private Vector3 loadVec3(Json5Array from) {
        return new Vector3(from.get(0).getAsFloat(), from.get(1).getAsFloat(), from.get(2).getAsFloat());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private Map<CubicDirection, FaceElement> loadFaces(Json5Object faces, int textureWidth, int textureHeight) {
        Map<CubicDirection, FaceElement> faceElems = new HashMap<>();
        for (Map.Entry<String, Json5Element> face : faces.entrySet()) {
            CubicDirection cubicDirection = CubicDirection.valueOf(face.getKey().toUpperCase(Locale.ROOT));
            Json5Object faceData = face.getValue().getAsJson5Object();
            Json5Array uvs = faceData.getAsJson5Array("uv");
            String texture = faceData.get("texture").getAsString();
            Json5Element rotation1 = faceData.get("rotation");
            int rotation = rotation1 == null ? 0 : rotation1.getAsInt();
            Json5Element tintIndex1 = faceData.get("tintindex");
            int tintIndex = tintIndex1 == null ? -1 : tintIndex1.getAsInt();
            Json5Element cullface = faceData.get("cullface");
            String cullFace = cullface == null ? null : cullface.getAsString();

            faceElems.put(cubicDirection, new FaceElement(texture, new UVs(uvs.get(0).getAsInt(), uvs.get(1).getAsInt(), uvs.get(2).getAsInt(), uvs.get(3).getAsInt(), textureWidth, textureHeight), rotation, tintIndex, cullFace));
        }

        return faceElems;
    }

    private Map<String, NamespaceID> loadTextures(Json5Object textures) {
        Map<String, NamespaceID> textureElements = new HashMap<>();

        for (var entry : textures.entrySet()) {
            String name = entry.getKey();
            String stringId = entry.getValue().getAsString();
            NamespaceID id = NamespaceID.parse(stringId).mapPath(path -> "textures/" + path + ".png");
            textureElements.put(name, id);
        }

        return textureElements;
    }

    public BlockModel load(RegistryKey<?> key, NamespaceID id) {
        Resource resource = this.resourceManager.getResource(id.mapPath(path -> "models/" + path + ".json5"));
        if (resource != null) {
            return this.load(key, resource.loadJson5());
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
        private final Map<CubicDirection, FaceElement> blockFaceFaceElementMap;
        private final boolean shade;
        private final ElementRotation rotation;
        private final Vector3 from;
        private final Vector3 to;


            public ModelElement(Map<CubicDirection, FaceElement> blockFaceFaceElementMap, boolean shade, ElementRotation rotation, Vector3 from, Vector3 to) {
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

            public void bake(int idx, ModelBuilder modelBuilder, Map<String, NamespaceID> textureElements) {
                Vector3 from = this.from();
                Vector3 to = this.to();

                ModelBuilder nodeBuilder = new ModelBuilder();
                nodeBuilder.begin();

                MeshBuilder meshBuilder = new MeshBuilder();
                VertexInfo v00 = new VertexInfo();
                VertexInfo v01 = new VertexInfo();
                VertexInfo v10 = new VertexInfo();
                VertexInfo v11 = new VertexInfo();
                for (Map.Entry<CubicDirection, FaceElement> entry : blockFaceFaceElementMap.entrySet()) {
                    CubicDirection cubicDirection = entry.getKey();
                    FaceElement faceElement = entry.getValue();
                    String texRef = faceElement.texture;
                    NamespaceID texture;

                    if (texRef.equals("#missing")) texture = new NamespaceID("textures/block/error.png");
                    else if (texRef.startsWith("#")) texture = textureElements.get(texRef.substring(1));
                    else texture = NamespaceID.parse(texRef).mapPath(path -> "textures/" + path + ".png");

                    meshBuilder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)), GL20.GL_TRIANGLES);
                    v00.setCol(Color.WHITE);
                    v01.setCol(Color.WHITE);
                    v10.setCol(Color.WHITE);
                    v11.setCol(Color.WHITE);

                    v00.setNor(cubicDirection.getNormal());
                    v01.setNor(cubicDirection.getNormal());
                    v10.setNor(cubicDirection.getNormal());
                    v11.setNor(cubicDirection.getNormal());

                    v00.setUV(faceElement.uvs.x1, faceElement.uvs.y2);
                    v01.setUV(faceElement.uvs.x1, faceElement.uvs.y1);
                    v10.setUV(faceElement.uvs.x2, faceElement.uvs.y2);
                    v11.setUV(faceElement.uvs.x2, faceElement.uvs.y1);

                    switch (cubicDirection) {
                        case UP:
                            v00.setPos(to.x, to.y, from.z);
                            v01.setPos(to.x, to.y, to.z);
                            v10.setPos(from.x, to.y, from.z);
                            v11.setPos(from.x, to.y, to.z);
                            break;
                        case DOWN:
                            v00.setPos(from.x, from.y, from.z);
                            v01.setPos(from.x, from.y, to.z);
                            v10.setPos(to.x, from.y, from.z);
                            v11.setPos(to.x, from.y, to.z);
                            break;
                        case WEST:
                            v00.setPos(from.x, from.y, from.z);
                            v01.setPos(from.x, to.y, from.z);
                            v10.setPos(from.x, from.y, to.z);
                            v11.setPos(from.x, to.y, to.z);
                            break;
                        case EAST:
                            v00.setPos(to.x, from.y, to.z);
                            v01.setPos(to.x, to.y, to.z);
                            v10.setPos(to.x, from.y, from.z);
                            v11.setPos(to.x, to.y, from.z);
                            break;
                        case NORTH:
                            v00.setPos(to.x, from.y, from.z);
                            v01.setPos(to.x, to.y, from.z);
                            v10.setPos(from.x, from.y, from.z);
                            v11.setPos(from.x, to.y, from.z);
                            break;
                        case SOUTH:
                            v00.setPos(from.x, from.y, to.z);
                            v01.setPos(from.x, to.y, to.z);
                            v10.setPos(to.x, from.y, to.z);
                            v11.setPos(to.x, to.y, to.z);
                            break;
                    }

                    meshBuilder.rect(v00, v10, v11, v01);

                    Material material = new Material();
                    material.set(TextureAttribute.createDiffuse(QuantumClient.get().getTextureManager().getTexture(texture)));
                    material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
                    material.set(new FloatAttribute(FloatAttribute.AlphaTest));
                    material.set(new DepthTestAttribute(GL20.GL_LEQUAL));
                    nodeBuilder.part(idx + "." + cubicDirection.name(), meshBuilder.end(), GL20.GL_TRIANGLES, material);
                }

                Model end = nodeBuilder.end();
                Node node = modelBuilder.node("[" + idx + "]", end);

                Vector3 originVec = rotation.originVec;
                Axis axis = rotation.axis;
                float angle = rotation.angle;
                boolean rescale = rotation.rescale; // TODO: implement

                node.localTransform.translate(originVec.x, originVec.y, originVec.z);
                node.localTransform.rotate(axis.getVector(), angle);
                node.localTransform.translate(-originVec.x, -originVec.y, -originVec.z);
                node.scale.set(node.localTransform.getScale(tmp));
                node.translation.set(node.localTransform.getTranslation(tmp));
                node.rotation.set(node.localTransform.getRotation(tmpQ));
            }

        public Map<CubicDirection, FaceElement> blockFaceFaceElementMap() {
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

    public static final class ElementRotation {
        private final Vector3 originVec;
        private final Axis axis;
        private final float angle;
        private final boolean rescale;

        public ElementRotation(Vector3 originVec, Axis axis, float angle, boolean rescale) {
            this.originVec = originVec;
            this.axis = axis;
            this.angle = angle;
            this.rescale = rescale;
        }

            public static ElementRotation deserialize(@Nullable Json5Object rotation) {
                if (rotation == null) {
                    return new ElementRotation(new Vector3(0, 0, 0), Axis.Y, 0, false);
                }

                Json5Array origin = rotation.getAsJson5Array("origin");
                String axis = rotation.get("axis").getAsString();
                float angle = rotation.get("angle").getAsFloat();
                Json5Element rescale1 = rotation.get("rescale");
                boolean rescale = rescale1 != null && rescale1.getAsBoolean();

                Vector3 originVec = new Vector3(origin.get(0).getAsFloat(), origin.get(1).getAsFloat(), origin.get(2).getAsFloat());
                return new ElementRotation(originVec, Axis.valueOf(axis.toUpperCase(Locale.ROOT)), angle, rescale);
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

        @Override
        public String toString() {
            return "ElementRotation[" +
                   "originVec=" + originVec + ", " +
                   "axis=" + axis + ", " +
                   "angle=" + angle + ", " +
                   "rescale=" + rescale + ']';
        }

        }

    public static final class Display {
        public Display() {
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
