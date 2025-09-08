package dev.ultreon.quantum.client.render.modes.advanced;

import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.shaders.provider.SkyboxShaders;
import dev.ultreon.quantum.client.shaders.provider.WorldShaderProvider;
import dev.ultreon.quantum.client.util.Toggleable;
import dev.ultreon.quantum.util.NamespaceID;

class AdvancedShaders implements Toggleable {
    public ShaderProvider skyBox;
    public ShaderProvider celestialBodies;
    public ShaderProvider transparent;
    public ShaderProvider water;
    public ShaderProvider opaque;
    public ShaderProvider gizmo;
    public ShaderProvider gizmoOutline;
    public ShaderProvider entityTransparent;
    public ShaderProvider cutout;

    public void enable() {
        opaque = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/vibrant/scene.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/vibrant/scene.frag")), "330 core", "Opaque (advanced)");
        transparent = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/vibrant/transparent.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/vibrant/transparent.frag")), "330 core", "Transparent (advanced)");
        entityTransparent = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/vibrant/transparent.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/vibrant/transparent.frag")), "330 core", "Transparent (advanced)");
        celestialBodies = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/vibrant/celestial_body.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/vibrant/celestial_body.frag")), "330 core", "Celestial Bodies (advanced)");
        water = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/vibrant/water.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/vibrant/water.frag")), "330 core", "Water (advanced)");
        cutout = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/vibrant/cutout.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/vibrant/cutout.frag")), "330 core", "Cutout (advanced)");
        skyBox = new SkyboxShaders(QuantumClient.resource(NamespaceID.of("shaders/vibrant/skybox.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/vibrant/skybox.frag")), "330 core", "Skybox (advanced)");
    }

    public void disable() {

    }
}
