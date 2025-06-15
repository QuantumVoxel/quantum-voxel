package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import dev.ultreon.quantum.client.render.PBRRenderBufferSource;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.util.GameCamera;
import net.mgsx.gltf.scene3d.attributes.PBRMatrixAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneRenderableSorter;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.scene.Updatable;
import net.mgsx.gltf.scene3d.shaders.PBRCommon;
import net.mgsx.gltf.scene3d.utils.EnvironmentCache;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Convient manager class for: model instances, animators, camera, environment, lights, batch/shaderProvider
 *
 * @author mgsx - Modified for use with Quantum Voxel by Qubix
 */
@SuppressWarnings({"GDXJavaUnsafeIterator", "ConstantValue"})
public class ShadowRenderer implements Disposable {

    private final Array<RenderableProvider> renderableProviders = new Array<>();

    private RenderBufferSource batch;
    private final RenderBufferSource depthBatch;
    private SceneSkybox skyBox;

    /**
     * Shouldn't be null.
     */
    public Environment environment = new Environment();
    protected final EnvironmentCache computedEnvironement = new EnvironmentCache();

    public GameCamera camera;

    private final RenderableSorter renderableSorter;

    private final PointLightsAttribute pointLights = new PointLightsAttribute();
    private final SpotLightsAttribute spotLights = new SpotLightsAttribute();

    public ShadowRenderer() {
        this(new SceneRenderableSorter());
    }

    public ShadowRenderer(RenderableSorter renderableSorter) {
        this.renderableSorter = renderableSorter;

        batch = new PBRRenderBufferSource(PBRRenderBufferSource.Mode.NORMAL);

        depthBatch = new PBRRenderBufferSource(PBRRenderBufferSource.Mode.DEPTH);

        float lum = 1f;
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, lum, lum, lum, 1));
    }

    public void setEnvironmentRotation(float azymuthAngleDegree) {
        PBRMatrixAttribute attribute = environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
        if (attribute != null) {
            attribute.set(azymuthAngleDegree);
        } else {
            environment.set(PBRMatrixAttribute.createEnvRotation(azymuthAngleDegree));
        }
    }

    public void removeEnvironmentRotation() {
        environment.remove(PBRMatrixAttribute.EnvRotation);
    }

    public RenderBufferSource getBatch() {
        return batch;
    }

    public void setBatch(RenderBufferSource batch) {
        this.batch = batch;
    }

    public void addScene(Scene scene) {
        addScene(scene, true);
    }

    public void addScene(Scene scene, boolean appendLights) {
        renderableProviders.add(scene);
        if (appendLights) {
            for (Entry<Node, BaseLight> e : scene.lights) {
                environment.add(e.value);
            }
        }
    }

    /**
     * should be called in order to perform light culling, skybox update and animations.
     *
     * @param delta
     */
    public void update(float delta) {
        if (camera != null) {
            updateEnvironment();
            for (RenderableProvider r : renderableProviders) {
                if (r instanceof Updatable) {
                    ((Updatable) r).update(camera, delta);
                }
            }
            if (skyBox != null) skyBox.update(camera, delta);
        }
    }

    /**
     * Automatically set skybox rotation matching this environement rotation.
     * Subclasses could override this method in order to change this behavior.
     */
    protected void updateSkyboxRotation() {
        if (skyBox != null) {
            PBRMatrixAttribute rotationAttribute = environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
            if (rotationAttribute != null) {
                skyBox.setRotation(rotationAttribute.matrix);
            }
        }
    }

    protected void updateEnvironment() {
        updateSkyboxRotation();

        computedEnvironement.setCache(environment);
        pointLights.lights.clear();
        spotLights.lights.clear();
        if (environment != null) {
            for (Attribute a : environment) {
                if (a instanceof PointLightsAttribute) {
                    pointLights.lights.addAll(((PointLightsAttribute) a).lights);
                    computedEnvironement.replaceCache(pointLights);
                } else if (a instanceof SpotLightsAttribute) {
                    spotLights.lights.addAll(((SpotLightsAttribute) a).lights);
                    computedEnvironement.replaceCache(spotLights);
                } else {
                    computedEnvironement.set(a);
                }
            }
        }
        cullLights();
    }

    protected void cullLights() {
        PointLightsAttribute pla = environment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
        if (pla != null) {
            for (PointLight light : pla.lights) {
                if (light instanceof PointLightEx) {
                    PointLightEx l = (PointLightEx) light;
                    if (l.range != null && !camera.frustum.sphereInFrustum(l.position, l.range)) {
                        pointLights.lights.removeValue(l, true);
                    }
                }
            }
        }
        SpotLightsAttribute sla = environment.get(SpotLightsAttribute.class, SpotLightsAttribute.Type);
        if (sla != null) {
            for (SpotLight light : sla.lights) {
                if (light instanceof SpotLightEx) {
                    SpotLightEx l = (SpotLightEx) light;
                    if (l.range != null && !camera.frustum.sphereInFrustum(l.position, l.range)) {
                        spotLights.lights.removeValue(l, true);
                    }
                }
            }
        }
    }

    /**
     * render all scenes.
     * because shadows use frame buffers, if you need to render scenes to a frame buffer, you should instead
     * first call {@link #renderShadows(Consumer)}, bind your frame buffer and then call {@link #renderColors(BiConsumer, Consumer)}
     */
    public void render(Consumer<RenderBufferSource> shadowBufferConsumer, BiConsumer<RenderBufferSource, Environment> colorBufferConsumer, Consumer<RenderBufferSource> skyboxBufferConsumer) {
        if (camera == null) return;

        renderShadows(shadowBufferConsumer);

        renderColors(colorBufferConsumer, skyboxBufferConsumer);
    }

    /**
     * Render shadows only to interal frame buffers.
     * (useful when you're using your own frame buffer to render scenes)
     */
    public void renderShadows(Consumer<RenderBufferSource> bufferConsumer) {
        DirectionalLight light = getFirstDirectionalLight();
        if (light instanceof DirectionalShadowLight) {
            DirectionalShadowLight shadowLight = (DirectionalShadowLight) light;
            shadowLight.begin();
            renderDepth(shadowLight.getCamera(), bufferConsumer);
            shadowLight.end();

            environment.shadowMap = shadowLight;
        } else {
            environment.shadowMap = null;
        }
    }

    /**
     * Render only depth (packed 32 bits), usefull for post processing effects.
     * You typically render it to a FBO with depth enabled.
     */
    public void renderDepth(Consumer<RenderBufferSource> bufferConsumer) {
        renderDepth(camera, bufferConsumer);
    }

    private void renderDepth(Camera camera, Consumer<RenderBufferSource> bufferConsumer) {
        depthBatch.begin(camera);
        bufferConsumer.accept(depthBatch);
        depthBatch.end();
    }

    /**
     * Render colors only. You should call {@link #renderShadows(Consumer)} before.
     * (useful when you're using your own frame buffer to render scenes)
     */
    public void renderColors(BiConsumer<RenderBufferSource, Environment> bufferConsumer, Consumer<RenderBufferSource> skyboxConsumer) {
        PBRCommon.enableSeamlessCubemaps();
        computedEnvironement.shadowMap = environment.shadowMap;
        if (camera == null) return;
        batch.begin(camera);
        bufferConsumer.accept(batch, computedEnvironement);
        if (skyBox != null) skyboxConsumer.accept(batch);
        batch.end();
    }

    public @Nullable DirectionalLight getFirstDirectionalLight() {
        DirectionalLightsAttribute dla = environment.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
        if (dla != null) {
            for (DirectionalLight dl : dla.lights) {
                if (dl instanceof DirectionalLight) {
                    return dl;
                }
            }
        }
        return null;
    }

    public void setSkyBox(SceneSkybox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneSkybox getSkyBox() {
        return skyBox;
    }

    public void setAmbientLight(float lum) {
        environment.get(ColorAttribute.class, ColorAttribute.AmbientLight).color.set(lum, lum, lum, 1);
    }

    public void setCamera(GameCamera camera) {
        this.camera = camera;
    }

    public void removeScene(Scene scene) {
        renderableProviders.removeValue(scene, true);
        for (Entry<Node, BaseLight> e : scene.lights) {
            environment.remove(e.value);
        }
    }

    public Array<RenderableProvider> getRenderableProviders() {
        return renderableProviders;
    }

    public void updateViewport(float width, float height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update(true);
        }
    }

    public int getActiveLightsCount() {
        return EnvironmentUtil.getLightCount(computedEnvironement);
    }

    public int getTotalLightsCount() {
        return EnvironmentUtil.getLightCount(environment);
    }


    @Override
    public void dispose() {
        batch.dispose();
        depthBatch.dispose();
    }
}
