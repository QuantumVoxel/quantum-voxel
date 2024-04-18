package com.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.model.ModelImporter;
import com.ultreon.quantum.resources.Resource;
import com.ultreon.quantum.util.Color;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.world.CubicDirection;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec4f;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BlockBenchModelImporter implements ModelImporter {
    private static final Matrix4 TEMP_MATRIX = new Matrix4();
    private static final Vector3 TEMP_VEC3 = new Vector3();
    private static final Quaternion TEMP_ROTATION = new Quaternion();
    private final BBMeta meta;
    private final String name;
    private final String modelIdentifier;
    private final Vec3f visibleBox;
    private final Vec2f resolution;
    private final List<BBModelElement> elements;
    private final Identifier id;
    private final BBModelOutliner outliner;
    private final List<BBAnimation> animations;
    private final List<BBTexture> textures;
    private Model model;

    public BlockBenchModelImporter(Identifier id) {
        this.id = id;
        QuantumClient client = QuantumClient.get();
        Resource resource = client.getResourceManager().getResource(id);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + id);
        }

        JsonObject jsonObject = resource.loadJson(JsonObject.class);
        this.meta = loadMeta(jsonObject.getAsJsonObject("meta"));
        this.name = jsonObject.getAsJsonPrimitive("name").getAsString();
        this.modelIdentifier = jsonObject.getAsJsonPrimitive("model_identifier").getAsString();

        this.visibleBox = loadVec3(jsonObject.getAsJsonArray("visible_box"));

        this.resolution = loadVec2Size(jsonObject.getAsJsonObject("resolution"));
        this.elements = loadElements(jsonObject.getAsJsonArray("elements"));

        this.outliner = loadOutliner(jsonObject.getAsJsonArray("outliner"));
        this.textures = loadTextures(jsonObject.getAsJsonArray("textures"));
        JsonArray animations1 = jsonObject.getAsJsonArray("animations");
        if (animations1 == null) {
            animations1 = new JsonArray();
        }
        this.animations = loadAnimations(animations1);
    }

    private Vec2f loadVec2Size(JsonObject resolution) {
        return new Vec2f(resolution.get("width").getAsFloat(), resolution.get("height").getAsFloat());
    }

    private List<BBAnimation> loadAnimations(JsonArray animations) {
        List<BBAnimation> list = new ArrayList<>();
        for (JsonElement elem : animations) {
//            list.add(loadAnimation(elem.getAsJsonObject()));
        }
        return list;
    }

    private List<BBTexture> loadTextures(JsonArray textures) {
        List<BBTexture> list = new ArrayList<>();
        for (JsonElement elem : textures) {
            list.add(loadTexture(elem.getAsJsonObject()));
        }
        return list;
    }

    private BBTexture loadTexture(JsonObject textureJson) {
        String path = textureJson.getAsJsonPrimitive("path").getAsString();
        String name = textureJson.getAsJsonPrimitive("name").getAsString();
        String folder = textureJson.getAsJsonPrimitive("folder").getAsString();
        String namespace = textureJson.getAsJsonPrimitive("namespace").getAsString();
        String id = textureJson.getAsJsonPrimitive("id").getAsString();
        int width = textureJson.getAsJsonPrimitive("width").getAsInt();
        int height = textureJson.getAsJsonPrimitive("height").getAsInt();
        int uvWidth = textureJson.getAsJsonPrimitive("uv_width").getAsInt();
        int uvHeight = textureJson.getAsJsonPrimitive("uv_height").getAsInt();
        boolean particle = textureJson.getAsJsonPrimitive("particle").getAsBoolean();
        boolean layersEnabled = textureJson.getAsJsonPrimitive("layers_enabled").getAsBoolean();
        String syncToProject = textureJson.getAsJsonPrimitive("sync_to_project").getAsString();
        String renderMode = textureJson.getAsJsonPrimitive("render_mode").getAsString();
        String renderSides = textureJson.getAsJsonPrimitive("render_sides").getAsString();
        int frameTime = textureJson.getAsJsonPrimitive("frame_time").getAsInt();
        String frameOrderType = textureJson.getAsJsonPrimitive("frame_order_type").getAsString();
        String frameOrder = textureJson.getAsJsonPrimitive("frame_order").getAsString();
        boolean frameInterpolate = textureJson.getAsJsonPrimitive("frame_interpolate").getAsBoolean();

        boolean visible = textureJson.getAsJsonPrimitive("visible").getAsBoolean();
        boolean internal = textureJson.getAsJsonPrimitive("internal").getAsBoolean();
        boolean saved = textureJson.getAsJsonPrimitive("saved").getAsBoolean();

        UUID uuid = UUID.fromString(textureJson.getAsJsonPrimitive("uuid").getAsString());
        String relativePath = textureJson.getAsJsonPrimitive("relative_path").getAsString();

        URL source = null;
        try {
            source = new URL(textureJson.getAsJsonPrimitive("source").getAsString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Base64.Decoder decoder = Base64.getDecoder();
        return new BBTexture(path, name, folder, namespace, id, width, height, uvWidth, uvHeight, particle, layersEnabled, syncToProject, renderMode, renderSides, frameTime, frameOrderType, frameOrder, frameInterpolate, visible, internal, saved, uuid, relativePath, source);
    }

    private BBModelOutliner loadOutliner(JsonArray outliner) {
        List<BBModelOutlineInfo> values = new ArrayList<>();

        for (JsonElement elem : outliner) {
            BBModelOutlineInfo node;
            node = loadNode(elem);
            if (node == null) {
                QuantumClient.LOGGER.warn("Failed to load BlockBench model node: " + elem);
            }
            values.add(node);
        }

        return new BBModelOutliner(values);
    }

    @Nullable
    private BBModelOutlineInfo loadNode(JsonElement elem) {
        BBModelOutlineInfo node;
        if (!elem.isJsonObject()) {
            if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                return new BBModelElementReference(UUID.fromString(elem.getAsString()));
            } else {
                return null;
            }
        } else {
            JsonObject elemObj = elem.getAsJsonObject();
            String name = elemObj.getAsJsonPrimitive("name").getAsString();
            Vec3f origin = loadVec3(elemObj.getAsJsonArray("origin"));
            Color color = loadColor(elemObj.getAsJsonPrimitive("color").getAsInt());
            UUID uuid = UUID.fromString(elemObj.getAsJsonPrimitive("uuid").getAsString());
            boolean export = elemObj.getAsJsonPrimitive("export").getAsBoolean();
            boolean mirrorUV = elemObj.getAsJsonPrimitive("mirror_uv").getAsBoolean();
            boolean isOpen = elemObj.getAsJsonPrimitive("isOpen").getAsBoolean();
            boolean visibility = elemObj.getAsJsonPrimitive("visibility").getAsBoolean();
            int autouv = elemObj.getAsJsonPrimitive("autouv").getAsInt();
            JsonArray rotation1 = elemObj.getAsJsonArray("rotation");
            List<BBModelOutlineInfo> children = new ArrayList<>();

            for (JsonElement child : elemObj.getAsJsonArray("children")) {
                children.add(loadNode(child));
            }

            if (rotation1 == null) return new BBModelGroup(this, name, origin, color, uuid, export, mirrorUV, isOpen, visibility, autouv, children, new Vec3f());
            Vec3f rotation = loadVec3(rotation1);
            return new BBModelGroup(this, name, origin, color, uuid, export, mirrorUV, isOpen, visibility, autouv, children, rotation);
        }
    }

    private List<BBModelElement> loadElements(JsonArray elements) {
        List<BBModelElement> processed = new ArrayList<>();
        for (JsonElement elem : elements) {
            if (!elem.isJsonObject()) continue;
            JsonObject elemObj = elem.getAsJsonObject();
            String type = elemObj.getAsJsonPrimitive("type").getAsString();
            switch (type) {
                case "cube" -> processed.add(loadCubeElement(elemObj.getAsJsonObject()));
                case "mesh" -> processed.add(loadMeshElement(elemObj.getAsJsonObject()));
            }
        }

        if (processed.isEmpty()) {
            QuantumClient.LOGGER.warn("BlockBench model {} has no elements", this.id);
        }

        return processed;
    }

    private BBMeshModelElement loadMeshElement(JsonObject meshJson) {
        String name = meshJson.getAsJsonPrimitive("name").getAsString();
        Color color = loadColor(meshJson.getAsJsonPrimitive("color").getAsInt());
        Vec3f origin = loadVec3(meshJson.getAsJsonArray("origin"));
        Vec3f rotation = loadVec3(meshJson.getAsJsonArray("rotation"));
        boolean export = meshJson.getAsJsonPrimitive("export").getAsBoolean();
        boolean visibility = meshJson.getAsJsonPrimitive("visibility").getAsBoolean();
        boolean locked = meshJson.getAsJsonPrimitive("locked").getAsBoolean();
        String renderOrder = meshJson.getAsJsonPrimitive("render_order").getAsString();
        boolean allowMirrorModeling = meshJson.getAsJsonPrimitive("allow_mirror_modeling").getAsBoolean();

        Map<String, BBModelVertex> vertices = loadVertices(meshJson.getAsJsonObject("vertices"));

        List<BBModelMeshFace> faces = loadMeshFaces(vertices, meshJson.getAsJsonObject("faces"));

        UUID uuid = UUID.fromString(meshJson.getAsJsonPrimitive("uuid").getAsString());

        return new BBMeshModelElement(name, color, origin, rotation, export, visibility, locked, renderOrder, allowMirrorModeling, faces, uuid);
    }

    private List<BBModelMeshFace> loadMeshFaces(Map<String, BBModelVertex> vertices, JsonObject faces) {
        List<BBModelMeshFace> processed = new ArrayList<>();
        for (Map.Entry<String, JsonElement> elem : faces.entrySet()) {
            processed.add(this.loadMeshFace(vertices, elem.getValue().getAsJsonObject()));
        }

        return processed;
    }

    private BBModelMeshFace loadMeshFace(Map<String, BBModelVertex> verticesRef, JsonObject asJsonObject) {
        Map<String, Vec2f> uvs = new HashMap<>();
        JsonObject uvJson = asJsonObject.getAsJsonObject("uv");

        List<BBModelVertex> vertices = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : uvJson.entrySet()) {
            uvs.put(entry.getKey(), loadVec2(entry.getValue().getAsJsonArray()));
        }

        JsonArray verticesJson = asJsonObject.getAsJsonArray("vertices");
        for (JsonElement elem : verticesJson) {
            vertices.add(verticesRef.get(elem.getAsString()));
            if (!uvs.containsKey(elem.getAsString()))
                throw new IllegalArgumentException("Missing uv for vertex: " + elem.getAsString());
        }

        for (Map.Entry<String, Vec2f> entry : uvs.entrySet())
            if (!vertices.contains(verticesRef.get(entry.getKey())))
                throw new IllegalArgumentException("Missing vertex for uv: " + entry.getKey());

        return new BBModelMeshFace(Collections.unmodifiableMap(uvs), Collections.unmodifiableList(vertices), asJsonObject.get("texture").getAsInt());
    }

    private Map<String, BBModelVertex> loadVertices(JsonObject vertices) {
        Map<String, BBModelVertex> processed = new HashMap<>();
        for (Map.Entry<String, JsonElement> elem : vertices.entrySet()) {
            processed.put(elem.getKey(), this.loadVertex(elem.getValue().getAsJsonArray()));
        }

        return processed;
    }

    private BBModelVertex loadVertex(JsonArray coords) {
        return new BBModelVertex(loadVec3(coords));
    }

    private List<Vec3f> loadVertices(JsonArray vertices) {
        List<Vec3f> processed = new ArrayList<>();
        for (JsonElement elem : vertices) {
            processed.add(loadVec3(elem.getAsJsonArray()));
        }

        return processed;
    }

    private BBCubeModelElement loadCubeElement(JsonElement elem) {
        String name = elem.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
        boolean boxUv = elem.getAsJsonObject().getAsJsonPrimitive("box_uv").getAsBoolean();
        boolean rescale = elem.getAsJsonObject().getAsJsonPrimitive("rescale").getAsBoolean();
        boolean locked = elem.getAsJsonObject().getAsJsonPrimitive("locked").getAsBoolean();
        String renderOrder = elem.getAsJsonObject().getAsJsonPrimitive("render_order").getAsString();
        boolean allowMirrorModeling = elem.getAsJsonObject().getAsJsonPrimitive("allow_mirror_modeling").getAsBoolean();
        Vec3f from = loadVec3(elem.getAsJsonObject().getAsJsonArray("from"));
        Vec3f to = loadVec3(elem.getAsJsonObject().getAsJsonArray("to"));
        float autouv = elem.getAsJsonObject().getAsJsonPrimitive("autouv").getAsFloat();
        Color color = loadColor(elem.getAsJsonObject().getAsJsonPrimitive("color").getAsInt());
        Vec3f origin = loadVec3(elem.getAsJsonObject().getAsJsonArray("origin"));

        List<BBModelFace> faces = loadFaces(elem.getAsJsonObject().getAsJsonObject("faces"));

        UUID uuid = UUID.fromString(elem.getAsJsonObject().getAsJsonPrimitive("uuid").getAsString());

        JsonArray rotation1 = elem.getAsJsonObject().getAsJsonArray("rotation");
        if (rotation1 == null) return new BBCubeModelElement(name, boxUv, rescale, locked, renderOrder, allowMirrorModeling, from, to, autouv, color, origin, faces, uuid);
        
        Vec3f rotation = loadVec3(rotation1);
        return new BBCubeModelElement(name, boxUv, rescale, locked, renderOrder, allowMirrorModeling, from, to, autouv, color, origin, faces, uuid, rotation);
    }

    private List<BBModelFace> loadFaces(JsonObject faces) {
        List<BBModelFace> processed = new ArrayList<>();
        for (Map.Entry<String, JsonElement> face : faces.entrySet()) {
            processed.add(loadFace(face));
        }

        return processed;
    }

    private BBModelFace loadFace(Map.Entry<String, JsonElement> faceData) {
        String face = faceData.getKey();
        CubicDirection blockFace = CubicDirection.valueOf(face.toUpperCase(Locale.ROOT));

        JsonObject faceJson = faceData.getValue().getAsJsonObject();
        Vec4f uv = loadVec4(faceJson.getAsJsonArray("uv"));
        JsonElement texture1 = faceJson.get("texture");
        int texture = texture1 == null ? -1 : texture1.getAsInt();

        return new BBModelFace(blockFace, uv, texture);
    }

    private Color loadColor(int color) {
        return Color.BLACK;
    }

    private Vec2f loadVec2(JsonArray array) {
        return new Vec2f(array.get(0).getAsFloat(), array.get(1).getAsFloat());
    }

    private Vec3f loadVec3(JsonArray array) {
        return new Vec3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    private Vec4f loadVec4(JsonArray array) {
        return new Vec4f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat());
    }

    private BBMeta loadMeta(JsonObject jsonObject) {
        String formatVersion = jsonObject.getAsJsonPrimitive("format_version").getAsString();
        BBModelFormat modelFormat = BBModelFormat.valueOf(jsonObject.getAsJsonPrimitive("model_format").getAsString().toUpperCase(Locale.ROOT));
        boolean boxUv = jsonObject.getAsJsonPrimitive("box_uv").getAsBoolean();

        return new BBMeta(formatVersion, modelFormat, boxUv);
    }

    public BBMeta getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public Vec3f getVisibleBox() {
        return visibleBox;
    }

    public Vec2f getResolution() {
        return resolution;
    }

    public List<BBModelElement> getElements() {
        return elements;
    }

    public Identifier getId() {
        return id;
    }

    public BBModelOutliner getOutliner() {
        return outliner;
    }

    public List<BBAnimation> getAnimations() {
        return animations;
    }

    public Model getModel() {
        return model;
    }

    public Model createModel() {
        ModelBuilder model = new ModelBuilder();
        model.begin();
        Map<Integer, BBTexture> texture2texture = new HashMap<>();
        for (int i = 0; i < textures.size(); i++) {
            System.out.println(textures.get(i).id());
            texture2texture.put(i, textures.get(i));
        }

        Map<UUID, ModelBuilder> subNodes = new HashMap<>();
//        for (BBModelElement element : elements) {
//            writeElement(model, subNodes, texture2texture, element);
//        }

        List<BBModelOutlineInfo> data = outliner.entries();
        extracted(data, null, model, subNodes, texture2texture);

        for (Map.Entry<Integer, BBTexture> builder : texture2texture.entrySet()) {
            QuantumClient.invokeAndWait(() -> {
                try {
                    System.out.println(textures.get(builder.getKey()).id());

//                    return model.part(textures.get(builder.getKey()).id(), builder.getValue().end(), GL20.GL_TRIANGLES, Utils.make(new Material(), (mat) -> {
//                        try {
//                            mat.set(TextureAttribute.createDiffuse(textures.get(builder.getKey()).loadOrGetTexture()));
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }));
                } catch (Exception e) {
                    throw new RuntimeException("Texture " + textures.get(builder.getKey()).id() + " failed to load", e);
                }

            });
        }

        this.model = model.end();
        this.model.calculateTransforms();
        return this.model;
    }

    private void extracted(List<BBModelOutlineInfo> data, BBModelGroup parent, ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes0, Map<Integer, BBTexture> texture2texture) {
        for (BBModelOutlineInfo node : data) {
            if (node instanceof BBModelGroup group) {
                group.parent = parent;
                Vec3f origin = group.origin();
                if (origin.x != 0 || origin.y != 0 || origin.z != 0) {
                    ModelBuilder wrapperBuilder = new ModelBuilder();
                    wrapperBuilder.begin();
                    writeGroup(wrapperBuilder, subNodes0, texture2texture, group);
                    Node node1 = groupBuilder.node(group.name() + "::PivotWrapper", wrapperBuilder.end());
                    node1.localTransform.translate(origin.x, origin.y, origin.z);
                    Vec3f rotation = this.chain(group, BBModelNode::rotation, (a, b) -> a.add(b));
                    node1.localTransform.rotate(Vector3.X, rotation.x);
                    node1.localTransform.rotate(Vector3.Y, rotation.y);
                    node1.localTransform.rotate(Vector3.Z, rotation.z);
                    node1.localTransform.translate(-origin.x, -origin.y, -origin.z);

                    node1.translation.set(node1.localTransform.getTranslation(new Vector3()));
                    node1.rotation.set(node1.localTransform.getRotation(new Quaternion()));
                    node1.scale.set(node1.localTransform.getScale(new Vector3()));
                } else {
                    writeGroup(groupBuilder, subNodes0, texture2texture, group);
                }
            } else if (node instanceof BBModelElementReference ref) {
                writeElement(groupBuilder, parent, subNodes0, texture2texture, ref);
            }
        }
    }

    private static Vec3f createPivotPoint(Vec3f origin) {
        return origin.cpy().neg();
    }

    private void writeElement(ModelBuilder groupBuilder, BBModelGroup group, Map<UUID, ModelBuilder> subNodes0, Map<Integer, BBTexture> texture2texture, BBModelElementReference ref) {
        BBModelElement element = this.getElement(ref.uuid());
        if (element == null) {
            throw new RuntimeException("Element not found: " + ref.uuid());
        }
        element.parent = group;

        Vec3f origin = element.origin();
        if (origin.x != 0 || origin.y != 0 || origin.z != 0) {
            Vec3f cpy = createPivotPoint(origin);
            ModelBuilder wrapperBuilder = new ModelBuilder();
            wrapperBuilder.begin();
            writeElement(groupBuilder, subNodes0, texture2texture, element);
            groupBuilder.node(element.name() + "::PivotWrapper", wrapperBuilder.end());
        } else {
            writeElement(groupBuilder, subNodes0, texture2texture, element);
        }
    }

    private void writeGroup(ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes0, Map<Integer, BBTexture> texture2texture, BBModelGroup group) {
        List<BBModelOutlineInfo> children = group.children();

        ModelBuilder nodeBuilder = new ModelBuilder();
        nodeBuilder.begin();
        extracted(children, group, nodeBuilder, subNodes0, texture2texture);
        Model childModel = nodeBuilder.end();
        Node written = groupBuilder.node(group.name(), childModel);

        Vec3f origin = group.origin();

        written.localTransform.translate(-origin.x, -origin.y, -origin.z);
        Vec3f rotation = this.chain(group, BBModelNode::rotation, (a, b) -> a.add(b));
        written.localTransform.rotate(Vector3.X, rotation.x);
        written.localTransform.rotate(Vector3.Y, rotation.y);
        written.localTransform.rotate(Vector3.Z, rotation.z);
        written.localTransform.translate(origin.x, origin.y, origin.z);

        written.translation.set(written.localTransform.getTranslation(new Vector3()));
        written.rotation.set(written.localTransform.getRotation(new Quaternion()));
        written.scale.set(written.localTransform.getScale(new Vector3()));
    }

    private void writeElement(ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes, Map<Integer, BBTexture> texture2builder, BBModelElement element) {
        Node written = element.write(groupBuilder, subNodes, texture2builder, this, resolution);
        if (written != null) {
//            written.rotation.set(rotationMatrix.getRotation(TEMP_ROTATION));
            if (element.origin().x != 0 || element.origin().y != 0 || element.origin().z != 0) {
                Vector3 origin;
                if (element instanceof BBCubeModelElement cube) {
                    origin = new Vector3(cube.from().x / 16f - element.origin().x / 16f, cube.from().y / 16f - element.origin().y / 16f, cube.from().z / 16f - element.origin().z / 16f);
                } else {
                    origin = new Vector3(element.origin().x / 16f, element.origin().y / 16f, element.origin().z / 16f);
                }
            }
        }
    }

    private <T> T chain(BBModelNode group, Function<BBModelNode, T> func, BiFunction<T, T, T> func2) {
        List<BBModelNode> groups = new ArrayList<>();
        groups.add(group);
        while (group.parent() != null) {
            group = group.parent();
            groups.add(group);
        }
        T sum = func.apply(group);
        for (BBModelNode g : groups) {
            sum = func2.apply(sum, func.apply(g));
        }
        return sum;
    }

    public BBModelElement getElement(UUID uuid) {
        return elements.stream().filter(e -> e.uuid().equals(uuid)).findFirst().orElse(null);
    }
}
