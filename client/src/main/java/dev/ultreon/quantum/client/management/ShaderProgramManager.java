package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.resources.ResourceFileHandle;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ShaderProgramManager implements Manager<ShaderProgram> {
    private final Map<NamespaceID, ShaderProgram> programs = new HashMap<>();
    private final Map<NamespaceID, Supplier<ShaderProgram>> factories = new HashMap<>();

    @Override
    public ShaderProgram register(@NotNull NamespaceID id, @NotNull ShaderProgram program) {
        this.programs.put(id, program);
        return program;
    }

    public Supplier<ShaderProgram> register(NamespaceID id, Supplier<ShaderProgram> factory) {
        Supplier<ShaderProgram> supplier = () -> {
            if (this.programs.containsKey(id))
                return this.programs.get(id);

            ShaderProgram program = factory.get();
            this.programs.put(id, program);
            return program;
        };

        this.factories.put(id, supplier);
        return supplier;
    }

    @Override
    public @Nullable ShaderProgram get(NamespaceID id) {
        ShaderProgram program = this.programs.get(id);

        if (program != null)
            return program;

        ShaderProgram shaderProgram = new ShaderProgram(
                new ResourceFileHandle(id.mapPath(p -> "shaders/" + p + ".vert")),
                new ResourceFileHandle(id.mapPath(p -> "shaders/" + p + ".frag"))
        );

        if (!shaderProgram.isCompiled())
            throw new RuntimeException("Failed to compile shader program: " + shaderProgram.getLog());

        if (!shaderProgram.getLog().isEmpty())
            QuantumClient.LOGGER.warn("Warning while compiling shader program: {}", shaderProgram.getLog());

        this.register(id, shaderProgram);
        program = shaderProgram;

        return program;
    }

    @Override
    public void reload(ReloadContext context) {
        for (ShaderProgram shader : List.copyOf(this.programs.values())) {
            context.submit(shader::dispose);
        }

        this.programs.clear();

        this.factories.forEach((id, factory) -> {
            ShaderProgram program = factory.get();
            this.programs.put(id, program);
            context.submit(program::dispose);
        });
    }
}
