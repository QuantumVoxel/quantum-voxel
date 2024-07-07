package dev.ultreon.quantum.client;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.util.MathHelper;

public class ShadowExample extends ApplicationAdapter {
    private ModelBatch modelBatch;
    private Environment environment;
    private ModelInstance modelInstance;
    private Camera camera;
    private DirectionalShadowLight shadowLight;
    private ModelInstance modelInstance2;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));

        // Create a directional shadow light
        shadowLight = new DirectionalShadowLight(1024, 1024, 60f, 60f, 0.001f, 1000f);
        shadowLight.set(Color.WHITE, new Vector3(-1, -0.5f, 0));
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;

        // Load your model
        ModelBuilder modelBuilder = new ModelBuilder();
        Model model = modelBuilder.createBox(5f, 5f, 5f,
                    new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        modelInstance = new ModelInstance(model);
        modelInstance.transform.translate(-5f, 0f, 0f);

        model = modelBuilder.createBox(5f, 5f, 5f,
                    new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        modelInstance2 = new ModelInstance(model);

        // Set up your camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 100f;
        camera.update();
    }

    @Override
    public void render() {
        shadowLight.direction.rotate(45f * Gdx.graphics.getDeltaTime(), 0f, 0f, 0f);
        shadowLight.update(camera);

        shadowLight.begin(camera);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(shadowLight.getCamera());
        modelBatch.render(modelInstance, environment);
        modelBatch.render(modelInstance2, environment);
        modelBatch.end();
        shadowLight.end();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        modelBatch.begin(camera);
        modelBatch.render(modelInstance, environment);
        modelBatch.render(modelInstance2, environment);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        modelInstance.model.dispose();
        shadowLight.dispose();
    }
}
