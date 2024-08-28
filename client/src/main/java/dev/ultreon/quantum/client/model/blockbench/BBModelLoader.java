package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3f;
import dev.ultreon.quantum.util.Vec4f;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.ModelImporter;
import dev.ultreon.quantum.client.model.blockbench.anim.*;
import dev.ultreon.quantum.resources.Resource;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.CubicDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BBModelLoader implements ModelImporter {
    private static final Matrix4 TEMP_MATRIX = new Matrix4();
    private static final Vector3 TEMP_VEC3 = new Vector3();
    private static final Quaternion TEMP_ROTATION = new Quaternion();
    private final BBMeta meta;
    private final String name;
    private final String modelIdentifier;
    private final Vec3f visibleBox;
    private final Vec2f resolution;
    private final List<BBModelElement> elements;
    private final NamespaceID id;
    private final BBModelOutliner outliner;
    private final List<BBAnimation> animations;
    private final List<BBTexture> textures;
    private Model model;

    public BBModelLoader(NamespaceID id) {
        this(id, BBModelLoader.getResource(id));
    }

    public BBModelLoader(NamespaceID id, Resource resource) {
        this.id = id;

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

    private static @NotNull Resource getResource(NamespaceID id) {
        QuantumClient client = QuantumClient.get();
        Resource resource = client.getResourceManager().getResource(id);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + id);
        }
        return resource;
    }

    private Vec2f loadVec2Size(JsonObject resolution) {
        return new Vec2f(resolution.get("width").getAsFloat(), resolution.get("height").getAsFloat());
    }

    private List<BBAnimation> loadAnimations(JsonArray animations) {
        List<BBAnimation> list = new ArrayList<>();
        for (JsonElement elem : animations) {
            list.add(loadAnimation(elem.getAsJsonObject()));
        }
        return list;
    }

    private BBAnimation loadAnimation(JsonObject asJsonObject) {
        UUID uuid = UUID.fromString(asJsonObject.get("uuid").getAsString());
        String name = asJsonObject.get("name").getAsString();
        BBAnimation.Loop loop = BBAnimation.Loop.valueOf(asJsonObject.get("loop").getAsString().toUpperCase());
        boolean override = asJsonObject.get("override").getAsBoolean();
        float length = asJsonObject.get("length").getAsFloat();
        int snapping = asJsonObject.get("snapping").getAsInt();
        boolean selected = asJsonObject.get("selected").getAsBoolean();
        String animTimeUpdate = asJsonObject.get("anim_time_update").getAsString();
        String blendWeight = asJsonObject.get("blend_weight").getAsString();
        String startDelay = asJsonObject.get("start_delay").getAsString();
        String loopDelay = asJsonObject.get("loop_delay").getAsString();

        JsonElement animatorsJson = asJsonObject.get("animators");
        Map<UUID, BBAnimator> animators = new HashMap<>();
        if (animatorsJson != null) {
            for (Map.Entry<String, JsonElement> entry : animatorsJson.getAsJsonObject().entrySet()) {
                animators.put(UUID.fromString(entry.getKey()), loadAnimator(entry.getValue().getAsJsonObject()));
            }
        }

        return new BBAnimation(uuid, name, loop, override, length, snapping, selected, animTimeUpdate, blendWeight, startDelay, loopDelay, animators);
    }

    private BBAnimator loadAnimator(JsonObject asJsonObject) {
        String name = asJsonObject.get("name").getAsString();
        BBAnimator.Type type = BBAnimator.Type.valueOf(asJsonObject.get("type").getAsString().toUpperCase());
        List<BBAnimKeyFrame> keyFrames = new ArrayList<>();
        JsonElement keyFramesJson = asJsonObject.get("keyframes");
        if (keyFramesJson != null) {
            for (JsonElement elem : keyFramesJson.getAsJsonArray()) {
                keyFrames.add(loadKeyFrame(elem.getAsJsonObject()));
            }
        }

        return new BBAnimator(name, type, keyFrames);
    }

    private BBAnimKeyFrame loadKeyFrame(JsonObject asJsonObject) {
        BBAnimChannel channel = BBAnimChannel.valueOf(asJsonObject.get("channel").getAsString().toUpperCase());
        List<Vector3> dataPoints = new ArrayList<>();
        for (JsonElement elem : asJsonObject.get("data_points").getAsJsonArray()) {
            dataPoints.add(loadVector3(elem.getAsJsonObject()));
        }

        UUID uuid = UUID.fromString(asJsonObject.get("uuid").getAsString());
        float time = asJsonObject.get("time").getAsFloat();
        int colorIdx = asJsonObject.get("color").getAsInt();
        BBColor color = colorIdx == -1 ? null : BBColor.values()[colorIdx];
        BBAnimInterpolation interpolation = BBAnimInterpolation.valueOf(asJsonObject.get("interpolation").getAsString().toUpperCase());
        boolean bezierLinked = asJsonObject.get("bezier_linked").getAsBoolean();
        Vector3 bezierLeftTime = loadVector3(asJsonObject.get("bezier_left_time").getAsJsonArray());
        Vector3 bezierLeftValue = loadVector3(asJsonObject.get("bezier_left_value").getAsJsonArray());
        Vector3 bezierRightTime = loadVector3(asJsonObject.get("bezier_right_time").getAsJsonArray());
        Vector3 bezierRightValue = loadVector3(asJsonObject.get("bezier_right_value").getAsJsonArray());

        return new BBAnimKeyFrame(channel, dataPoints, uuid, time, color, interpolation, bezierLinked, bezierLeftTime, bezierLeftValue, bezierRightTime, bezierRightValue);
    }

    private Vector3 loadVector3(JsonObject asJsonObject) {
        return new Vector3(asJsonObject.get("x").getAsFloat(), asJsonObject.get("y").getAsFloat(), asJsonObject.get("z").getAsFloat());
    }

    private Vector3 loadVector3(JsonArray asJsonArray) {
        return new Vector3(asJsonArray.get(0).getAsFloat(), asJsonArray.get(1).getAsFloat(), asJsonArray.get(2).getAsFloat());
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

        URI source;
        try {
            source = new URI(textureJson.getAsJsonPrimitive("source").getAsString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return new BBTexture(path, name, folder, namespace, id, width, height, uvWidth, uvHeight, particle, layersEnabled, syncToProject, renderMode, renderSides, frameTime, frameOrderType, frameOrder, frameInterpolate, visible, internal, saved, uuid, relativePath, source);
    }

    private BBModelOutliner loadOutliner(JsonArray outliner) {
        List<BBModelOutlineInfo> values = new ArrayList<>();

        for (JsonElement elem : outliner) {
            BBModelOutlineInfo node;
            node = loadNode(elem);
            if (node == null) {
                QuantumClient.LOGGER.warn("Failed to load BlockBench model node: {}", elem);
            }
            values.add(node);
        }

        return new BBModelOutliner(values);
    }

    @Nullable
    private BBModelOutlineInfo loadNode(JsonElement elem) {
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
            RgbColor color = loadColor(elemObj.getAsJsonPrimitive("color").getAsInt());
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
                case "cube":
                    processed.add(loadCubeElement(elemObj.getAsJsonObject()));
                    break;
                case "mesh":
                    processed.add(loadMeshElement(elemObj.getAsJsonObject()));
                    break;
            }
        }

        if (processed.isEmpty()) {
            QuantumClient.LOGGER.warn("BlockBench model {} has no elements", this.id);
        }

        return processed;
    }

    private BBMeshModelElement loadMeshElement(JsonObject meshJson) {
        String name = meshJson.getAsJsonPrimitive("name").getAsString();
        RgbColor color = loadColor(meshJson.getAsJsonPrimitive("color").getAsInt());
        Vec3f origin = loadVec3(meshJson.getAsJsonArray("origin"));
        Vec3f rotation = loadVec3(meshJson.getAsJsonArray("rotation"));
        boolean export = meshJson.getAsJsonPrimitive("export").getAsBoolean();
        boolean visibility = meshJson.getAsJsonPrimitive("visibility").getAsBoolean();
        boolean locked = meshJson.getAsJsonPrimitive("locked").getAsBoolean();
        String renderOrder = meshJson.getAsJsonPrimitive("render_order").getAsString();
        boolean allowMirrorModeling = meshJson.getAsJsonPrimitive("allow_mirror_modeling").getAsBoolean();

        LinkedHashMap<String, BBModelVertex> vertices = loadVertices(meshJson.getAsJsonObject("vertices"));

        List<BBModelMeshFace> faces = loadMeshFaces(vertices, meshJson.getAsJsonObject("faces"));

        UUID uuid = UUID.fromString(meshJson.getAsJsonPrimitive("uuid").getAsString());

        return new BBMeshModelElement(name, color, origin, rotation, export, visibility, locked, renderOrder, allowMirrorModeling, faces, uuid);
    }

    private List<BBModelMeshFace> loadMeshFaces(LinkedHashMap<String, BBModelVertex> vertices, JsonObject faces) {
        List<BBModelMeshFace> processed = new ArrayList<>();
        for (Map.Entry<String, JsonElement> elem : faces.entrySet()) {
            BBModelMeshFace e = this.loadMeshFace(vertices, elem.getValue().getAsJsonObject());
            if (e != null) processed.add(e);
        }

        return processed;
    }

    private BBModelMeshFace loadMeshFace(Map<String, BBModelVertex> verticesRef, JsonObject asJsonObject) {
        Map<String, Vec2f> uvs = new HashMap<>();
        JsonObject uvJson = asJsonObject.getAsJsonObject("uv");

        List<BBModelVertex> vertices = new ArrayList<>();
        List<Vector2> mappedUVs = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : uvJson.entrySet()) {
            uvs.put(entry.getKey(), loadVec2(entry.getValue().getAsJsonArray()));
        }

        JsonArray verticesJson = asJsonObject.getAsJsonArray("vertices");
        for (JsonElement elem : verticesJson) {
            vertices.add(verticesRef.get(elem.getAsString()));
            if (!uvs.containsKey(elem.getAsString()))
                throw new IllegalArgumentException("Missing uv for vertex: " + elem.getAsString());
        }

        for (Map.Entry<String, Vec2f> entry : uvs.entrySet()) {
            Vec2f vec2f = uvs.get(entry.getKey());
            mappedUVs.add(new Vector2(vec2f.x, vec2f.y));

            if (!vertices.contains(verticesRef.get(entry.getKey())))
                throw new IllegalArgumentException("Missing vertex for uv: " + entry.getKey());
        }

        JsonElement texture = asJsonObject.get("texture");
        if (texture == null) {
            QuantumClient.LOGGER.warn("BlockBench model {} has a mesh face with no texture", this.id);
            return null;
        }
        return new BBModelMeshFace(mappedUVs, Collections.unmodifiableList(vertices), texture.getAsInt());
    }

    private LinkedHashMap<String, BBModelVertex> loadVertices(JsonObject vertices) {
        LinkedHashMap<String, BBModelVertex> processed = new LinkedHashMap<>();
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
        RgbColor color = loadColor(elem.getAsJsonObject().getAsJsonPrimitive("color").getAsInt());
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

    private RgbColor loadColor(int color) {
        return RgbColor.BLACK;
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

    public NamespaceID getId() {
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

        Map<UUID, Node> nodes = new HashMap<>();

        List<BBModelOutlineInfo> data = outliner.entries();
        extracted(data, null, model, subNodes, nodes, texture2texture);

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


        this.model = QuantumClient.invokeAndWait(model::end);

        for (BBAnimation animation : animations) {
            Animation animations = animation.create(nodes, this);
            this.model.animations.add(animations);
        }

        this.model.calculateTransforms();
        return this.model;
    }

    private void extracted(List<BBModelOutlineInfo> data, BBModelGroup parent, ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes0, Map<UUID, Node> nodes, Map<Integer, BBTexture> texture2texture) {
        for (BBModelOutlineInfo node : data) {
            if (node instanceof BBModelGroup group) {
                group.parent = parent;
                ModelBuilder wrapperBuilder = new ModelBuilder();
                wrapperBuilder.begin();

                Vec3f rotation = group.rotation();
                Vec3f pivot = group.origin();

                writeGroup(wrapperBuilder, subNodes0, nodes, texture2texture, group);
                Node wrapper = groupBuilder.node(group.name(), wrapperBuilder.end());

                wrapper.translation.set(pivot.x / 16f, pivot.y / 16f, pivot.z / 16f);
                wrapper.rotation.setEulerAngles(rotation.y, rotation.x, rotation.z);

                wrapper.calculateLocalTransform();

                nodes.put(group.uuid(), wrapper);
            } else if (node instanceof BBModelElementReference ref) {
                writeElement(groupBuilder, parent, subNodes0, nodes, texture2texture, ref);
            }
        }
    }

    private void writeElement(ModelBuilder groupBuilder, BBModelGroup group, Map<UUID, ModelBuilder> subNodes0, Map<UUID, Node> nodes, Map<Integer, BBTexture> texture2texture, BBModelElementReference ref) {
        BBModelElement element = this.getElement(ref.uuid());
        if (element == null) {
            throw new RuntimeException("Element not found: " + ref.uuid());
        }

        element.parent = group;

        ModelBuilder wrapperBuilder = new ModelBuilder();
        wrapperBuilder.begin();

        Vec3f pivot = element.origin();
        Vec3f rotation = element.rotation();

        writeElement(groupBuilder, subNodes0, nodes, texture2texture, element);
        Node wrapper = groupBuilder.node(element.name(), wrapperBuilder.end());

        wrapper.translation.set(pivot.x / 16f, pivot.y / 16f, pivot.z / 16f);
        wrapper.rotation.setEulerAngles(rotation.y, rotation.x, rotation.z);

        wrapper.calculateLocalTransform();

        nodes.put(element.uuid(), wrapper);
    }

    private void writeGroup(ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes0, Map<UUID, Node> nodes, Map<Integer, BBTexture> texture2texture, BBModelGroup group) {
        List<BBModelOutlineInfo> children = group.children();

        ModelBuilder nodeBuilder = new ModelBuilder();
        nodeBuilder.begin();
        extracted(children, group, nodeBuilder, subNodes0, nodes, texture2texture);
        Model childModel = nodeBuilder.end();
        Node wrapped = groupBuilder.node(group.name() + "::PivotWrapped", childModel);

        Vec3f pivot = group.origin();
        wrapped.translation.set(-pivot.x / 16f, -pivot.y / 16f, -pivot.z / 16f);
    }

    private Node writeElement(ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes, Map<UUID, Node> nodes, Map<Integer, BBTexture> texture2builder, BBModelElement element) {
        Node written = element.write(groupBuilder, subNodes, texture2builder, this, resolution);
        if (written != null) {
            Vec3f pivot = element.origin();
            written.translation.set(-pivot.x / 16f, -pivot.y / 16f, -pivot.z / 16f);
            return written;
        }

        throw new RuntimeException("Element not written: " + element);
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

    private Vec3f relativize(BBModelNode group, Function<BBModelNode, Vec3f> func) {
        List<BBModelNode> groups = new ArrayList<>();
        groups.add(group);
        while (group.parent() != null) {
            group = group.parent();
            groups.add(group);
        }
        Vec3f sum = func.apply(group).cpy();
        for (BBModelNode g : groups)
            sum.add(func.apply(g).cpy().sub(sum));

        return sum;
    }

    public BBModelElement getElement(UUID uuid) {
        return elements.stream().filter(e -> e.uuid().equals(uuid)).findFirst().orElse(null);
    }
}
