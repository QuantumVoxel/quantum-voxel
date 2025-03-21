package dev.ultreon.quantum.client.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.util.PlayerView;
import dev.ultreon.quantum.client.world.WorldRenderer;
import dev.ultreon.quantum.debug.DebugFlags;
import dev.ultreon.quantum.debug.inspect.InspectionNode;
import dev.ultreon.quantum.util.BlockHit;
import dev.ultreon.quantum.util.Ray;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.util.Vec3f;
import lombok.Getter;

/**
 * The camera used for the game.
 * Originates at 0,0,0. The world is rendered relative to the camera.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class GameCamera extends PerspectiveCamera {
    public final QuantumClient client = QuantumClient.get();
    public float fov = 67;
    private float fovModifier = 1;
    private float fovModifierGoal = 1;
    private InspectionNode<GameCamera> node;
    private Vector3 hitPosition;
    private Vec3d camPos;
    private BlockHit hitResult;
    private LocalPlayer player;
    private float cameraBop;
    private boolean inverseBop;
    private boolean walking;
    private static final Vector3 TMP_1 = new Vector3();
    private static final Vector3 TMP_2 = new Vector3();

    public GameCamera(float fieldOfViewY, float viewportWidth, float viewportHeight) {
        super(fieldOfViewY, viewportWidth, viewportHeight);

        if (DebugFlags.INSPECTION_ENABLED.isEnabled()) {
            this.node = this.client.inspection.createNode("camera", () -> this);
            this.node.create("position", () -> this.position);
            this.node.create("direction", () -> this.direction);
            this.node.create("up", () -> this.up);
            this.node.create("near", () -> this.near);
            this.node.create("far", () -> this.far);
            this.node.create("viewportWidth", () -> this.viewportWidth);
            this.node.create("viewportHeight", () -> this.viewportHeight);
            this.node.create("fieldOfView", () -> this.fov);
            this.node.create("hitPosition", () -> this.hitResult.getVec());
            this.node.create("relHitPosition", () -> this.hitPosition);
            this.node.create("eyePosition", () -> this.camPos);
            this.node.create("playerPosition", () -> this.player.getPosition(client.partialTick));
        }
    }

    public float getFovModifier() {
        return fovModifier;
    }

    public void setFovModifier(float fovModifier) {
        this.fovModifierGoal = fovModifier;
    }

    /**
     * Updates the camera's position and direction based on the player's position and look vector.
     *
     * @param player the player to update the camera for.
     */
    public void update(LocalPlayer player) {
        var lookVec = player.getLookVector(client.partialTick);
        this.camPos = player.getPosition(client.partialTick).div(WorldRenderer.SCALE).add(0, player.getEyeHeight() / WorldRenderer.SCALE, 0);
        this.player = player;

        float deltaTime = Gdx.graphics.getDeltaTime();
        if (fovModifierGoal != fovModifier) {
            if (fovModifierGoal > fovModifier) {
                fovModifier += deltaTime * 12f * Math.abs(fovModifier - fovModifierGoal);
                if (fovModifier > fovModifierGoal) fovModifier = fovModifierGoal;
            } else {
                fovModifier -= deltaTime * 12f * Math.abs(fovModifier - fovModifierGoal);
                if (fovModifier < fovModifierGoal) fovModifier = fovModifierGoal;
            }
        }

        this.fieldOfView = fov * fovModifier;

        if (this.client.isInThirdPerson()) {
            this.updateThirdPerson(lookVec);
        } else {
//            this.updateThirdPerson(lookVec);
            if (DebugFlags.INSPECTION_ENABLED.isEnabled()) {
                this.node.remove("hitPosition");
                this.node.remove("eyePosition");
                this.node.remove("playerPosition");
            }
            // Set the camera's position to zero, and set the camera's direction to the player's look vector.
            this.position.set(0, 0, 0);
            this.direction.set((float) lookVec.x, (float) lookVec.y, (float) lookVec.z);
        }

        float duration = 0.5f;
        if (player.isWalking()) this.walking = true;
        if (!this.walking) this.cameraBop = 0;
        else this.updateWalkAnim(player, this.cameraBop, deltaTime, duration);

        super.update(true);
    }

    private void updateWalkAnim(LocalPlayer player, float cameraBop, float delta, float duration) {
        this.walking = true;
        float old = this.cameraBop;

        var bop = this.cameraBop;
        bop -= this.inverseBop ? delta : -delta;

        if (bop > duration) {
            float overflow = duration - bop;
            bop = duration - overflow;
            this.inverseBop = true;
        } else if (bop < -duration) {
            float overflow = duration + bop;
            bop = -duration - overflow;
            this.inverseBop = false;
        }

        if (!player.isWalking() && (old >= 0 && cameraBop < 0 || old <= 0 && cameraBop > 0)) {
            player.walking = false;
        }

        this.cameraBop = bop * 6;
    }

    private void updateThirdPerson(Vec3d lookVec) {
        // Move camera backwards when player is in third person.
        var ray = new Ray(this.camPos, lookVec.cpy().neg().nor());
        var world = this.client.world;
        if (world != null) {
            if (client.getPlayerView() == PlayerView.THIRD_PERSON) {
                this.hitResult = world.rayCast(ray, 5.1f);
                this.direction.set((float) lookVec.x, (float) lookVec.y, (float) lookVec.z);
                if (this.hitResult.isCollide()) {
                    Vec3f normal = this.hitResult.getNormal().f();
                    Vector3 gdxNormal = TMP_1.set(normal.x, normal.y, normal.z);
                    Vector3 hitOffset = TMP_2.set(this.direction).nor()
                            .scl(-this.hitResult.getDistance())
                            .sub(gdxNormal.scl(-0.1f).rotate(this.direction, 360));
                    this.hitPosition = TMP_1.set(0, 0, 0).add(hitOffset);
                } else {
                    this.hitPosition = TMP_1.set(this.direction).nor().scl(-5);
                }
                this.position.set(this.hitPosition.x, this.hitPosition.y, this.hitPosition.z);
            } else if (client.getPlayerView() == PlayerView.THIRD_PERSON_FRONT) {
                this.hitPosition = TMP_1.set(this.direction).nor().scl(-5.1f);
                this.direction.set(-(float) lookVec.x, -(float) lookVec.y, -(float) lookVec.z);
                if (this.hitResult.isCollide()) {
                    Vec3f normal = this.hitResult.getNormal().f();
                    Vector3 gdxNormal = TMP_1.set(normal.x, normal.y, normal.z);
                    Vector3 hitOffset = TMP_2.set(this.direction).nor()
                            .scl(-this.hitResult.getDistance())
                            .sub(gdxNormal.scl(-0.1f).rotate(this.direction, 360));
                    this.hitPosition = TMP_1.set(0, 0, 0).add(hitOffset);
                } else {
                    this.hitPosition = TMP_1.set(this.direction).nor().scl(-5);
                }
                this.position.set(this.hitPosition.x, this.hitPosition.y, this.hitPosition.z);
            }
        }
    }

    public Vector3 relative(Vec3d position, Vector3 tmp) {
        LocalPlayer localPlayer = this.client.player;
        if (localPlayer == null) return null;
        Vec3f sub = position.sub(this.player.getPosition(client.partialTick).add(0, this.player.getEyeHeight(), 0)).f();
        return tmp.set(sub.x, sub.y, sub.z);
    }

	public float getFov() {
		return fov;
	}

	public void setFov(float fov) {
		this.fov = fov;
	}

	public float getFovModifierGoal() {
		return fovModifierGoal;
	}

	public void setFovModifierGoal(float fovModifierGoal) {
		this.fovModifierGoal = fovModifierGoal;
	}

	public Vec3d getCamPos() {
		return camPos;
	}

	public void setCamPos(Vec3d camPos) {
		this.camPos = camPos;
	}

	public float getCameraBop() {
		return cameraBop;
	}

	public void setCameraBop(float cameraBop) {
		this.cameraBop = cameraBop;
	}
}
