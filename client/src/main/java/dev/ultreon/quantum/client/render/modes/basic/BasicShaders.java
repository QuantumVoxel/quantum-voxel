package dev.ultreon.quantum.client.render.modes.basic;

import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.shaders.provider.SkyboxShaders;
import dev.ultreon.quantum.client.shaders.provider.WorldShaderProvider;
import dev.ultreon.quantum.client.util.Toggleable;
import dev.ultreon.quantum.util.NamespaceID;

class BasicShaders implements Toggleable {
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
        opaque = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/scene.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/scene.frag")), null, "Opaque (basic)");
        transparent = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/transparent.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/transparent.frag")), null, "Transparent (basic)");
        entityTransparent = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/transparent.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/transparent.frag")), null, "Entity Transparent (basic)");
        celestialBodies = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/transparent.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/transparent.frag")), null, "Celestial Bodies (basic)");
        water = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/water.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/water.frag")), null, "Water (basic)");
        cutout = new WorldShaderProvider(QuantumClient.resource(NamespaceID.of("shaders/cutout.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/cutout.frag")), null, "Cutout (basic)");
        skyBox = new SkyboxShaders(QuantumClient.resource(NamespaceID.of("shaders/skybox.vert")),
                QuantumClient.resource(NamespaceID.of("shaders/skybox.frag")), null, "Skybox (basic)");
    }

    public void disable() {

    }
}
