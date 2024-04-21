package com.ultreon.quantum.client.init;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.ultreon.quantum.client.render.shader.OpenShaderProvider;

class MyDefaultShaderProvider extends DefaultShaderProvider implements OpenShaderProvider {
    public MyDefaultShaderProvider(DefaultShader.Config config) {
        super(config);
    }

    public MyDefaultShaderProvider(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDefaultShaderProvider(FileHandle vertexShader, FileHandle fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDefaultShaderProvider() {
    }

    @Override
    public Shader createShader(Renderable renderable) {
        return super.createShader(renderable);
    }

}
