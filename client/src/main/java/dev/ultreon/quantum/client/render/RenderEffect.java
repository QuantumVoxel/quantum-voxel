package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.google.common.base.Preconditions;
import dev.ultreon.mixinprovider.GeomShaderProgram;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Matrices;
import dev.ultreon.quantum.util.Identifier;

import java.util.function.Supplier;
import java.util.regex.Pattern;

public class RenderEffect {
    public static final RenderEffect CUTOUT = RenderEffect.register("cutout", RenderEffect.builder()
            .program(ShaderPrograms.EFFECT_CUTOUT)
            .build());
    public static final RenderEffect TRANSPARENT = RenderEffect.register("transparent", RenderEffect.builder()
            .program(ShaderPrograms.EFFECT_TRANSPARENT)
            .build());
    public static final RenderEffect GENERIC = RenderEffect.register("generic", RenderEffect.builder()
            .program(ShaderPrograms.EFFECT_GENERIC)
            .build());
    public static final RenderEffect WATER = RenderEffect.register("water", RenderEffect.builder()
            .program(ShaderPrograms.EFFECT_TRANSPARENT)
            .build());
    public static final Pattern PATTERN = Pattern.compile("u_([a-z][A-Za-z0-9])Texture");

    private final ThreadLocal<ShaderProgram> program;
    private final ThreadLocal<Mesh> mesh = new ThreadLocal<>();
    private final Matrix4 temp = new Matrix4();

    private RenderEffect(Supplier<? extends ShaderProgram> program) {
        this.program = ThreadLocal.withInitial(program);
    }

    private static RenderEffect register(String name, RenderEffect renderType) {
        ClientRegistries.RENDER_EFFECT.register(new Identifier(CommonConstants.NAMESPACE, name), renderType);
        return renderType;
    }

    private static Builder builder() {
        return new Builder();
    }

    public void begin(Mesh mesh, Matrices matrices, TextureSamplers samplers) {
        QuantumClient client = QuantumClient.get();
        ShaderProgram shader = this.program.get();
        int samplerId = 0;
        for (String uniform : shader.getUniforms()) {
            switch (uniform) {
                case "u_projViewTrans" -> shader.setUniformMatrix(uniform, client.camera.combined);
                case "u_viewTrans" -> shader.setUniformMatrix(uniform, client.camera.view);
                case "u_projTrans" -> shader.setUniformMatrix(uniform, client.camera.projection);
                case "u_cameraPosition" -> shader.setUniformf(uniform, client.camera.position);
                case "u_cameraDirection" -> shader.setUniformf(uniform, client.camera.direction);
                case "u_cameraUp" -> shader.setUniformf(uniform, client.camera.up);
                case "u_cameraNearFar" -> shader.setUniformf(uniform, client.camera.near, client.camera.far);
                case "u_cameraFov" -> shader.setUniformf(uniform, client.camera.fieldOfView);
                case "u_cameraViewWorldTrans" -> shader.setUniformMatrix(uniform, temp.set(client.camera.view).mul(matrices.last())););
                case "u_worldTrans" -> shader.setUniformMatrix(uniform, matrices.last());
                case "u_time" -> shader.setUniformf(uniform, QuantumClient.get().gameTime);
                default -> {
                    if (uniform.startsWith("u_") && uniform.endsWith("Texture")) {
                        Texture texture = samplers.get(uniform.substring(2, uniform.length() - 7));
                        int unit = samplerId++;
                        texture.bind(unit);

                        shader.setUniformi(uniform, unit);
                    }
                }
            }
        }

        mesh.bind(shader);
        this.mesh.set(mesh);
    }

    public void render(int mode) {
        this.mesh.get().render(this.program.get(), mode);
    }

    public void end() {
        Mesh mesh = this.mesh.get();
        if (mesh == null) return;
        mesh.unbind(this.program.get());
        this.program.remove();
        this.mesh.remove();
    }

    public static class Builder {
        private Supplier<? extends ShaderProgram> program;

        private Builder() {

        }

        public Builder program(Supplier<ShaderProgram> program) {
            this.program = program;
            return this;
        }

        public Builder geomProgram(Supplier<GeomShaderProgram> program) {
            this.program = program;
            return this;
        }

        public RenderEffect build() {
            Preconditions.checkNotNull(program, "Program not set!");
            return new RenderEffect(program);
        }
    }
}
