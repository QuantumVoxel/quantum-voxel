package dev.ultreon.mixinprovider;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class RenderDump {

    public static void dump(Renderable renderable) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("WorldTransform", gson.toJsonTree(renderable.worldTransform));
        Material material = renderable.material;
        jsonObject.add("MaterialId", gson.toJsonTree(material == null ? null : material.id));
        MeshPart meshPart = renderable.meshPart;
        jsonObject.add("MeshPartId", gson.toJsonTree(meshPart == null ? null : meshPart.id));
        jsonObject.add("Bones", gson.toJsonTree(renderable.bones));
        Shader shader = renderable.shader;
        jsonObject.addProperty("ShaderClass", shader == null ? null : shader.getClass().getName());
        Object userData = renderable.userData;
        jsonObject.addProperty("UserDataClass", userData == null ? null : userData.getClass().getName());
        String json = gson.toJson(jsonObject);

        Path output = Path.of("DUMP_" + UUID.randomUUID() + ".json");
        try {
            Files.writeString(output, json, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
