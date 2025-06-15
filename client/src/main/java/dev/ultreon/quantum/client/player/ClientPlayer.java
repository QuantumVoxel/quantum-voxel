package dev.ultreon.quantum.client.player;

import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.MathHelper;
import dev.ultreon.quantum.util.Vec;
import dev.ultreon.quantum.util.Vec3d;
import org.jetbrains.annotations.NotNull;

public abstract class ClientPlayer extends Player {
    private final QuantumClient client = QuantumClient.get();
    public float bodyXRot;
    public float bop;
    public boolean inverseBop;
    public float bopZ;
    public boolean inverseBopZ;
    private float xRot0;
    private float yRot0;
    private float xHeadRot0;
    float oXRot;
    float oYRot;
    float oXHeadRot;

    /**
     * Protected constructor for ClientPlayer that initializes the entity as a player in the client-side world.
     *
     * @param entityType The type of the player entity being created.
     * @param world The client world access object representing the world in which this player entity exists.
     */
    protected ClientPlayer(EntityType<? extends Player> entityType, ClientWorldAccess world) {
        super(entityType, world, QuantumClient.get().getUser().name());
    }

    @Override
    public void tick() {
        super.tick();

        this.client.camera.setFovModifier(((float) (getSpeed() / .2F) - 1.0F) * 0.05F + 1.0F);

        this.oXRot = this.xRot;
        this.oYRot = this.yRot;
        this.oXHeadRot = this.xHeadRot;
        this.xRot0 = this.xRot;
        this.yRot0 = this.yRot;
        this.xHeadRot0 = this.xHeadRot;
    }

    /**
     * Calculates the interpolated horizontal rotation of the player's head
     * based on the partial tick time.
     *
     * @param partialTick The partial tick time used for interpolation.
     * @return The interpolated head horizontal rotation.
     */
    public float getHeadXRot(float partialTick) {
        return MathHelper.lerp(partialTick, this.xHeadRot0, this.xHeadRot);
    }

    /**
     * Calculates and returns the interpolated rotation about the X-axis.
     *
     * @param partialTick The interpolation value ranging from 0.0 to 1.0.
     * @return The interpolated value for the X-axis rotation.
     */
    public float getXRot(float partialTick) {
        return MathHelper.lerp(partialTick, this.xRot0, this.xRot);
    }

    /**
     * Gets the interpolated Y-axis rotation based on a partial tick value.
     *
     * @param partialTick The partial tick value used for interpolation.
     * @return The interpolated Y-axis rotation.
     */
    public float getYRot(float partialTick) {
        return MathHelper.lerp(partialTick, this.yRot0, this.yRot);
    }

    /**
     * Interpolates the player's X coordinate between two values based on the provided partial tick.
     *
     * @param partialTick The interpolation factor, representing the fraction of time between ticks.
     * @return The interpolated X coordinate.
     */
    public double getX(float partialTick) {
        return MathHelper.lerp(partialTick, this.ox, this.x);
    }

    /**
     * Retrieves the interpolated Y position based on the given partial tick value.
     *
     * @param partialTick The fractional time between the previous and next tick.
     * @return The interpolated Y position.
     */
    public double getY(float partialTick) {
        return MathHelper.lerp(partialTick, this.oy, this.y);
    }

    /**
     * Returns the interpolated Z-coordinate value based on the given partial tick.
     *
     * @param partialTick A float value representing the partial tick for interpolation.
     * @return A double value representing the interpolated Z-coordinate.
     */
    public double getZ(float partialTick) {
        return MathHelper.lerp(partialTick, this.oz, this.z);
    }

    /**
     * Computes the interpolated position of the client player based on the partial tick value.
     *
     * @param partialTick The partial tick value used to interpolate the position.
     * @param vec The vector where the interpolated coordinates will be stored.
     * @return The vector containing the interpolated position.
     */
    public Vec getPosition(float partialTick, Vec vec) {
        vec.x = MathHelper.lerp(partialTick, this.ox, this.x);
        vec.y = MathHelper.lerp(partialTick, this.oy, this.y);
        vec.z = MathHelper.lerp(partialTick, this.oz, this.z);
        return vec;
    }

    /**
     * Checks if the client player has a specific explicit permission.
     *
     * @param permission The permission to check for the client player.
     * @return true if the client player has the specified explicit permission, false otherwise.
     */
    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return false;
    }

    /**
     * Causes the client player to drop the currently selected item from their inventory.
     * The actual logic for dropping the item is inherited from the superclass.
     */
    @Override
    public void dropItem() {
        super.dropItem();
    }

    /**
     * Computes the directional look vector of the player based on the partial tick time.
     *
     * @param partialTick The partial tick time used for interpolation, ranging from 0.0 to 1.0.
     * @return A normalized Vec3d representing the look direction of the player.
     */
    public Vec3d getLookVector(float partialTick) {
        // Calculate the direction vector
        Vec3d direction = new Vec3d();

        float yRot = this.getYRot(partialTick);
        float xHeadRot = this.getHeadXRot(partialTick);
        direction.x = (float) (Math.cos(Math.toRadians(yRot)) * Math.sin(Math.toRadians(xHeadRot)));
        direction.z = (float) (Math.cos(Math.toRadians(yRot)) * Math.cos(Math.toRadians(xHeadRot)));
        direction.y = (float) (Math.sin(Math.toRadians(yRot)));

        // Normalize the direction vector
        direction.nor();
        return direction;
    }
}
