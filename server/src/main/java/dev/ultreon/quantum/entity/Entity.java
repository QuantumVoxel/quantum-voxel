package dev.ultreon.quantum.entity;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.api.ModApi;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.api.events.entity.EntityMoveEvent;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.entity.util.EntitySize;
import dev.ultreon.quantum.network.packets.s2c.S2CEntityPipeline;
import dev.ultreon.quantum.network.packets.s2c.S2CPlayerPositionPacket;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.server.util.Utils;
import dev.ultreon.quantum.text.LanguageBootstrap;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.Translations;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity base class.
 * An entity is an object in the world that can freely move and can store data.
 * Entities can be spawned using the {@link World#spawn(Entity)} method and despawned using the {@link World#despawn(Entity)} method.
 * Or they can be marked for removal using the {@link #markRemoved()} method.
 * <p>
 * Entities can be loaded from a {@link MapType} object using the {@link #loadFrom(World, MapType)} method.
 * And they can be saved to a {@link MapType} object using the {@link #save(MapType)} method.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @see World#spawn(Entity)
 * @see <a href="https://github.com/Ultreon/quantum-voxel/wiki/Entities">Entities</a>
 */
public abstract class Entity extends GameObject implements CommandSender {
    private final EntityType<? extends Entity> type;
    protected WorldAccess world;
    public double x;
    public double y;
    public double z;
    public float xRot;
    public float yRot;
    private int id = -1;
    public boolean onGround;
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public float gravity = 0.08f;
    public float drag = 0.98f;
    public boolean noGravity;
    public boolean isColliding;
    public boolean isCollidingX;
    public boolean isCollidingY;
    public boolean isCollidingZ;

    public boolean noClip;
    protected double fallDistance = 0;
    public boolean wasInFluid = false;
    private boolean swimUp;
    protected double ox, oy, oz;
    private @Nullable String formatName;
    private @Nullable TextObject customName;
    private UUID uuid = Utils.ZEROED_UUID;
    protected AttributeMap attributes = new AttributeMap();
    private final RNG random = new JavaRNG();
    protected MapType pipeline = new MapType();
    private boolean markedForRemoval;
    private double oDx;
    private double oDy;
    private double oDz;
    private boolean wasOnGround;

    /**
     * Creates a new entity.
     *
     * @param entityType the entity type
     * @param world      the world to create the entity in
     */
    public Entity(EntityType<? extends Entity> entityType, WorldAccess world) {
        this.type = entityType;
        this.world = world;

        this.setupAttributes();
    }

    /**
     * Initializes the attributes for the entity.
     * This method is called during the entity setup process to configure
     * its attributes.
     */
    protected void setupAttributes() {

    }

    /**
     * Loads the entire entity data from a MapType object.
     *
     * @param world the world to load the entity in
     * @param data  the MapType object to load the data from
     * @return the loaded entity
     */
    public static @NotNull Entity loadFrom(World world, MapType data) {
        NamespaceID typeId = NamespaceID.parse(data.getString("type"));
        EntityType<?> type = Registries.ENTITY_TYPE.get(typeId);
        Entity entity = type.create(world);

        entity.id = data.getInt("id");

        entity.loadWithPos(data);
        return entity;
    }

    /**
     * Loads the data of the object including position from a MapType object.
     *
     * @param data the MapType object to load the data from
     */
    public void loadWithPos(MapType data) {
        MapType position = data.getMap("Position", new MapType());
        this.x = position.getDouble("x", this.x);
        this.y = position.getDouble("y", this.y);
        this.z = position.getDouble("z", this.z);

        this.load(data);
    }

    /**
     * Loads the data of the object including position, rotation, velocity, id, type, fall distance, gravity,
     * drag, noGravity, and noClip from a MapType object.
     *
     * @param data the MapType object to load the data from
     */
    public void load(MapType data) {
        MapType rotation = data.getMap("Rotation", new MapType());
        this.xRot = rotation.getFloat("x", this.xRot);
        this.yRot = rotation.getFloat("y", this.yRot);

        MapType velocity = data.getMap("Velocity", new MapType());
        this.velocityX = velocity.getDouble("x", this.velocityX);
        this.velocityY = velocity.getDouble("y", this.velocityY);
        this.velocityZ = velocity.getDouble("z", this.velocityZ);

        this.fallDistance = data.getDouble("fallDistance", this.fallDistance);
        this.gravity = data.getFloat("gravity", this.gravity);
        this.drag = data.getFloat("drag", this.drag);
        this.noGravity = data.getBoolean("noGravity", this.noGravity);
        this.noClip = data.getBoolean("noClip", this.noClip);
    }

    /**
     * Saves the data of the object including position, rotation, velocity, id, type, fall distance, gravity,
     * drag, noGravity, and noClip to a MapType object.
     *
     * @param data the MapType object to save the data to
     * @return the MapType object containing all the saved data
     */
    public MapType save(MapType data) {
        MapType position = new MapType();
        position.putDouble("x", this.x);
        position.putDouble("y", this.y);
        position.putDouble("z", this.z);
        data.put("Position", position);

        MapType rotation = new MapType();
        rotation.putFloat("x", this.xRot);
        rotation.putFloat("y", this.yRot);
        data.put("Rotation", rotation);

        MapType velocity = new MapType();
        velocity.putDouble("x", this.velocityX);
        velocity.putDouble("y", this.velocityY);
        velocity.putDouble("z", this.velocityZ);
        data.put("Velocity", velocity);

        data.putInt("id", this.id);
        data.putString("type", Objects.requireNonNull(Registries.ENTITY_TYPE.getId(this.type)).toString());

        data.putDouble("fallDistance", this.fallDistance);
        data.putFloat("gravity", this.gravity);
        data.putFloat("drag", this.drag);
        data.putBoolean("noGravity", this.noGravity);
        data.putBoolean("noClip", this.noClip);

        return data;
    }

    public EntitySize getSize() {
        return this.type.getSize();
    }

    /**
     * Marks the entity for removal.
     * When this is called the entity will be removed from the world in the next tick.
     * Removing players is not recommended, and should be avoided as it will cause the
     * player connection to bug out.
     *
     * @see #isMarkedForRemoval()
     * @see <a href="https://github.com/Ultreon/quantum-voxel/wiki/Entities#entity-removal">Entity Removal</a>
     */
    public void markRemoved() {
        this.markedForRemoval = true;
    }

    /**
     * Returns whether the entity is marked for removal.
     *
     * @return {@code true} if the entity is marked for removal, {@code false} otherwise
     * @see #markRemoved()
     * @see <a href="https://github.com/Ultreon/quantum-voxel/wiki/Entities#entity-removal">Entity Removal</a>
     */
    public boolean isMarkedForRemoval() {
        return this.markedForRemoval;
    }

    /**
     * Updates the entity's state and movement.
     */
    @Override
    public void tick() {
        // Apply gravity if not in the noGravity state
        if (!this.noGravity) {
            // If affected by fluid and swimming up, stop swimming up
            if (this.isAffectedByFluid() && this.swimUp) {
                this.swimUp = false;
            } else if (this.isAffectedByFluid()) {
                this.velocityY -= this.gravity / 3;
            } else if (!this.onGround) {
                // Apply gravity to the velocityY
                this.velocityY -= this.gravity;
            } else {
                this.velocityY = Math.max(0, this.velocityY);
            }
        }

        // Move the entity
        this.move();

        // Apply fluid drag if affected by fluid
        if (this.isAffectedByFluid()) {
            this.velocityX *= 0.56f;
            this.velocityY *= 0.56f;
            this.velocityZ *= 0.56f;
        } else {
            // Apply regular drag if not affected by fluid
            this.velocityX *= 0.6F;
            this.velocityY *= this.noGravity ? 0.6F : this.drag;
            this.velocityZ *= 0.6F;
        }

        // Slow down the entity on ground
        if (this.onGround) {
            this.velocityX *= 0.9f;
            this.velocityZ *= 0.9f;
        }

        if (!(this instanceof Player)) {
            sendPipeline();
        }
    }

    /**
     * Moves the entity by its current velocity.
     */
    protected void move() {
        this.move(this.velocityX, this.velocityY, this.velocityZ);
    }

    /**
     * Checks if the entity is affected by a fluid.
     *
     * @return true if the entity is in water, false otherwise
     */
    public boolean isAffectedByFluid() {
        return this.isInWater();
    }

    /**
     * Checks if the entity is currently in a block of water.
     *
     * @return true if the entity is in water, false otherwise
     */
    public boolean isInWater() {
        return this.world.get(this.getBlockVec()).isWater();
    }

    /**
     * Moves the entity upward if affected by fluid.
     */
    protected void swimUp() {
        // If affected by fluid, swim up
        if (this.isAffectedByFluid()) {
            this.swimUp = true;
        }

        // Check if the entity was previously in fluid
        if (!this.wasInFluid && this.isAffectedByFluid()) {
            this.wasInFluid = true;
        }
    }

    /**
     * Moves the entity by the specified deltas.
     *
     * @param deltaX the change in x-coordinate
     * @param deltaY the change in y-coordinate
     * @param deltaZ the change in z-coordinate
     * @return true if the entity is colliding after the move, false otherwise
     */
    public boolean move(double deltaX, double deltaY, double deltaZ) {
        // Trigger an event to allow modification of the move
        EntityMoveEvent event = new EntityMoveEvent(this, new Vec(deltaX, deltaY, deltaZ));
        ModApi.getGlobalEventHandler().call(event);
        Vec modifiedValue = event.getDelta();

        if (event.isCanceled()) {
            return this.isColliding;
        }

        // If the event is canceled and a modified value is provided, update the deltas
        deltaX = modifiedValue.x;
        deltaY = modifiedValue.y;
        deltaZ = modifiedValue.z;

        // Store the original deltas after potential modification
        double originalDeltaXModified = deltaX;
        double originalDeltaYModified = deltaY;
        double originalDeltaZModified = deltaZ;

        // Update the bounding box based on the modified deltas
        BoundingBox updatedBoundingBox = this.getBoundingBox().updateByDelta(deltaX, deltaY, deltaZ);

        // Move the entity based on the updated bounding box and deltas
        if (this.noClip) {
            this.x += deltaX;
            this.y += deltaY;
            this.z += deltaZ;
            this.onMoved();
            this.pipeline.putDouble("x", this.x);
            this.pipeline.putDouble("y", this.y);
            this.pipeline.putDouble("z", this.z);
        } else {
            this.moveWithCollision(updatedBoundingBox, deltaX, deltaY, deltaZ, originalDeltaXModified, originalDeltaYModified, originalDeltaZModified);
            this.onMoved();
            this.pipeline.putDouble("x", this.x);
            this.pipeline.putDouble("y", this.y);
            this.pipeline.putDouble("z", this.z);
        }

        return this.isColliding;
    }

    /**
     * Moves the entity with collision detection and response.
     *
     * @param ext   Bounding box of the entity
     * @param dx    Change in x-coordinate
     * @param dy    Change in y-coordinate
     * @param dz    Change in z-coordinate
     * @param oldDx Original change in x-coordinate
     * @param oldDy Original change in y-coordinate
     * @param oldDz Original change in z-coordinate
     */
    private void moveWithCollision(BoundingBox ext, double dx, double dy, double dz, double oldDx, double oldDy, double oldDz) {
        // Get list of bounding boxes the entity collides with
        List<BoundingBox> boxes = this.world.collide(ext, false);

        BoundingBox motionBox = this.getBoundingBox(); // Get the entity's bounding box

        this.isColliding = false;
        this.isCollidingY = false;

        // Check collision and update y-coordinate
        for (BoundingBox box : boxes) {
            double dy2 = BoundingBoxUtils.clipYCollide(box, motionBox, dy);
            boolean colliding = dy != dy2;
            this.isColliding |= colliding;
            this.isCollidingY |= colliding;
            if (colliding) {
                Object userData = box.userData;
                if (userData instanceof BlockCollision) {
                    BlockCollision collision = (BlockCollision) userData;
                    if (dy < 0) {
                        collision.getBlock().onWalkOn(this, collision, box, -dy);
                    }
                    collision.getBlock().onTouch(this, collision, box, Math.abs(dy));
                }
            }
            dy = dy2;
        }

        // Update the y-coordinate of the bounding box
        motionBox.min.add(0.0f, dy, 0.0f);
        motionBox.max.add(0.0f, dy, 0.0f);
        motionBox.update();

        this.isCollidingX = false;

        double heightToStep = 0;

        // Check collision and update x-coordinate
        for (BoundingBox box : boxes) {
            double dx2 = BoundingBoxUtils.clipXCollide(box, motionBox, dx);
            boolean colliding = dx != dx2;
            heightToStep = Math.max(heightToStep, box.max.y - motionBox.min.y);
            this.isColliding |= colliding;
            this.isCollidingX |= colliding;
            if (colliding) {
                Object userData = box.userData;
                if (userData instanceof BlockCollision) {
                    BlockCollision collision = (BlockCollision) userData;
                    collision.getBlock().onTouch(this, collision, box, Math.abs(dx));
                }
            }
            dx = dx2;
        }

        // Update the x-coordinate of the bounding box
        motionBox.min.add(dx, 0.0f, 0.0f);
        motionBox.max.add(dx, 0.0f, 0.0f);
        motionBox.update();

        this.isCollidingZ = false;

        // Check collision and update z-coordinate
        for (BoundingBox box : boxes) {
            double dz2 = BoundingBoxUtils.clipZCollide(box, motionBox, dz);
            boolean colliding = dz != dz2;
            heightToStep = Math.max(heightToStep, box.max.y - motionBox.min.y);
            this.isColliding |= colliding;
            this.isCollidingZ |= colliding;
            if (colliding) {
                Object userData = box.userData;
                if (userData instanceof BlockCollision) {
                    BlockCollision collision = (BlockCollision) userData;
                    collision.getBlock().onTouch(this, collision, box, Math.abs(dz));
                }
            }
            dz = dz2;
        }

        // Update the z-coordinate of the bounding box
        motionBox.min.add(0.0f, 0.0f, dz);
        motionBox.max.add(0.0f, 0.0f, dz);
        motionBox.update();

        if (heightToStep > 0 && heightToStep < 0.6 && onGround) {
            step(dx, dy, dz, heightToStep, motionBox);
            return;
        }

        // Check if entity is on the ground
        this.wasOnGround = this.onGround;
        this.onGround = oldDy != dy && oldDy < 0.0f;

        // Reset velocity if there was a collision in x-coordinate
        if (oldDx != dx) {
            this.velocityX = 0.0f;
        }

        // Reset fall distance if entity is moving upwards
        if (dy >= 0) {
            this.fallDistance = 0.0F;
        }

        dy = falling(dy);

        // Reset velocity if there was a collision in z-coordinate
        if (oldDz != dz) {
            this.velocityZ = 0.0f;
        }

        // Update entity's position
        this.x = (motionBox.min.x + motionBox.max.x) / 2.0f;
        this.y = motionBox.min.y;
        this.z = (motionBox.min.z + motionBox.max.z) / 2.0f;

        this.oDx = dx;
        this.oDy = dy;
        this.oDz = dz;
    }

    private void step(double dx, double dy, double dz, double heightToStep, BoundingBox motionBox) {
        this.velocityY = 0;
        this.isCollidingY = true;
        this.isColliding = true;
        this.isCollidingX = false;
        this.isCollidingZ = false;
        this.fallDistance = 0.0F;
        this.wasOnGround = this.onGround;
        this.onGround = true;

        falling(0);

        // Update entity's position
        this.x = (motionBox.min.x + motionBox.max.x) / 2.0f;
        this.y += heightToStep;
        this.z = (motionBox.min.z + motionBox.max.z) / 2.0f;

        this.oDx = dx;
        this.oDy = dy;
        this.oDz = dz;
    }

    private double falling(double dy) {
        // Handle collision responses and update fall distance
        if (this.isAffectedByFluid()) {
            wasInFluid = true;
            fallDistance = 0;
            dy = 0.0f;
        } else if (wasInFluid && !isAffectedByFluid()) {
            wasInFluid = false;
            fallDistance = 0;
            dy = 0.0f;
        } else if (this.onGround && !this.wasOnGround) {
            this.hitGround();
            this.fallDistance = 0.0F;
            this.velocityY = 0.0f;
            dy = 0.0f;
        } else if (dy < 0) {
            this.fallDistance -= dy;
        }
        return dy;
    }

    /**
     * Handles the entity movement.
     */
    protected void onMoved() {
        // Impl reasons
    }

    /**
     * Handles the entity ground hit.
     */
    protected void hitGround() {
        // Impl reasons
    }

    /**
     * @return true if the entity is in the void, false otherwise.
     * @deprecated not used until found a different use for void (feature? 👀).
     */
    @Deprecated
    public boolean isInVoid() {
        return false;
    }

    /**
     * Computes and returns the bounding box of the current object.
     *
     * @return the bounding box that encompasses the current object.
     */
    public BoundingBox getBoundingBox() {
        return this.getBoundingBox(this.getSize());
    }

    /**
     * Calculates and returns the bounding box for an entity based on its current position and size.
     *
     * @param size the size of the entity for which the bounding box is calculated
     * @return the bounding box corresponding to the entity's current position and size
     */
    @ApiStatus.OverrideOnly
    public BoundingBox getBoundingBox(EntitySize size) {
        return Entity.getBoundingBox(this.getPosition(), size);
    }

    /**
     * Calculates and returns a bounding box based on the provided position and size.
     *
     * @param pos  The central position of the bounding box as a {@code Vec3d}.
     * @param size The size of the entity, which includes width and height, as an {@code EntitySize}.
     * @return A {@code BoundingBox} object that represents the boundary defined by the dimensions centered on the given position.
     */
    public static BoundingBox getBoundingBox(Vec3d pos, EntitySize size) {
        double x1 = pos.x - size.width() / 2;
        double y1 = pos.y;
        double z1 = pos.z - size.width() / 2;
        double x2 = pos.x + size.width() / 2;
        double y2 = pos.y + size.height();
        double z2 = pos.z + size.width() / 2;
        return new BoundingBox(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2));
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getXRot() {
        return this.xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public float getYRot() {
        return this.yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = Mth.clamp(yRot, -90, 90);
    }

    public Vec3d getPosition() {
        return new Vec3d(this.x, this.y, this.z);
    }

    @ApiStatus.Internal
    public void setPosition(Vec3d position) {
        this.x = position.x;
        this.y = position.y;
        this.z = position.z;
        this.ox = position.x;
        this.oy = position.y;
        this.oz = position.z;
    }

    @ApiStatus.Internal
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.ox = x;
        this.oy = y;
        this.oz = z;

        if (this.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            if (this instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) this;
                serverWorld.sendAllTrackingExcept((int) this.x, (int) this.y, (int) this.z, new S2CPlayerPositionPacket(serverPlayer.getUuid(), getPosition()), serverPlayer);
            } else {
                serverWorld.sendAllTracking((int) this.x, (int) this.y, (int) this.z, new S2CEntityPipeline(this.getId(), getPipeline()));
            }
        }
    }

    /**
     * Retrieves a BlockVec instance representing the coordinates in the world space.
     *
     * @return a new BlockVec object with the current coordinates (x, y, z) and space set to WORLD.
     */
    public BlockVec getBlockVec() {
        return new BlockVec(this.x, this.y, this.z);
    }

    /**
     * Retrieves the current rotation values.
     *
     * @return a Vec2f object containing the rotation on the x and y axes.
     */
    public Vec2f getRotation() {
        return new Vec2f(this.xRot, this.yRot);
    }

    /**
     * Computes and returns the look vector based on the current rotation angles.
     *
     * @return A normalized Vec3d representing the direction in which the entity is looking.
     */
    public Vec3d getLookVector() {
        // Calculate the direction vector
        Vec3d direction = new Vec3d();

        this.yRot = Mth.clamp(this.yRot, -89.9F, 89.9F);
        direction.x = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.sin(Math.toRadians(this.xRot)));
        direction.z = (float) (Math.cos(Math.toRadians(this.yRot)) * Math.cos(Math.toRadians(this.xRot)));
        direction.y = (float) (Math.sin(Math.toRadians(this.yRot)));

        // Normalize the direction vector
        direction.nor();
        return direction;
    }

    /**
     * Sets the rotation of the entity based on the given position.
     *
     * @param position a Vec2f object containing the x and y rotation values
     */
    public void setRotation(Vec2f position) {
        this.xRot = position.x;
        this.yRot = Mth.clamp(position.y, -90, 90);

        if (this.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            if (this instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer) this;
                serverWorld.sendAllTrackingExcept((int) this.x, (int) this.y, (int) this.z, new S2CPlayerPositionPacket(serverPlayer.getUuid(), getPosition(), serverPlayer.xHeadRot, xRot, yRot), serverPlayer);
            } else {
                serverWorld.sendAllTracking((int) this.x, (int) this.y, (int) this.z, new S2CEntityPipeline(this.getId(), getPipeline()));
            }
        }
    }

    /**
     * Retrieves the current velocity vector.
     *
     * @return a Vec3d object representing the current velocity.
     */
    public Vec3d getVelocity() {
        return new Vec3d(this.velocityX, this.velocityY, this.velocityZ);
    }

    /**
     * Sets the velocity of an object using a Vec3d instance.
     *
     * @param velocity a Vec3d instance representing the new velocity
     *                 with components x, y, and z
     */
    public void setVelocity(Vec3d velocity) {
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
    }

    /**
     * This method is called during the preparation phase of spawning an entity.
     * Subclasses should override this method to add custom spawn preparation logic.
     *
     * @param spawnData the data related to the spawning process, providing necessary context and parameters
     */
    @ApiStatus.OverrideOnly
    public void onPrepareSpawn(MapType spawnData) {

    }

    /**
     * Teleports the entity to the specified coordinates.
     *
     * @param x the x-coordinate to teleport to
     * @param y the y-coordinate to teleport to
     * @param z the z-coordinate to teleport to
     */
    public void teleportTo(int x, int y, int z) {
        this.setPosition(x + 0.5, y, z + 0.5);
    }

    /**
     * Teleports the player to the specified coordinates.
     *
     * @param x the x-coordinate to teleport to
     * @param y the y-coordinate to teleport to
     * @param z the z-coordinate to teleport to
     */
    public void teleportTo(double x, double y, double z) {
        this.setPosition(x, y, z);
    }

    public WorldAccess getWorld() {
        return this.world;
    }

    public int getId() {
        return this.id;
    }

    @ApiStatus.Internal
    public void setId(int id) {
        this.id = id;
    }

    public float getGravity() {
        return this.gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void rotate(Vec2f rotation) {
        this.xRot = this.xRot + rotation.x;
        this.yRot = Mth.clamp(this.yRot + rotation.y, -90, 90);
    }

    public void rotate(float x, float y) {
        this.xRot = this.xRot + x;
        this.yRot = Mth.clamp(this.yRot + y, -90, 90);
    }

    public EntityType<?> getType() {
        return this.type;
    }

    /**
     * Retrieves the location of the entity.
     *
     * @return the location of the entity
     */
    @Override
    public @NotNull Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    /**
     * Retrieves the name of the entity.
     * This is official name of the entity, not the explicit public name.
     *
     * @return the name of the entity
     */
    @Override
    public String getName() {
        NamespaceID typeId = this.getType().getId();

        // If the type ID is null, return a default null object translation
        if (typeId == null) return "NULL";

        // Generate a display name based on the entity's type ID
        return LanguageBootstrap.translate(String.format("%s.entity.%s.name",
                typeId.getDomain(),
                typeId.getPath().replace('/', '.')
        ));
    }

    /**
     * Retrieves the public name for the entity.
     * This is the name that is publicly visible.
     * For example, in the chat (for players).
     *
     * @return the public name for the entity
     */
    @Override
    public @Nullable String getPublicName() {
        return this.getDisplayName().getText();
    }

    /**
     * Retrieves the display name for the entity.
     * If a custom name is set, it returns the custom name.
     * Otherwise, it uses the entity type translation.
     *
     * @return the display name for the entity
     */
    @Override
    public TextObject getDisplayName() {
        // Check if a custom name is set and return it if available
        if (this.customName != null) return this.customName;

        NamespaceID typeId = this.getType().getId();

        // If the type ID is null, return a default null object translation
        if (typeId == null) return Translations.NULL_OBJECT;

        // Generate a display name based on the entity's type ID
        return TextObject.translation(String.format("%s.entity.%s.name",
                typeId.getDomain(),
                typeId.getPath().replace('/', '.')
        ));
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public void sendMessage(@NotNull String message) {

    }

    @Override
    public void sendMessage(@NotNull TextObject component) {

    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return false;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public @Nullable QuantumServer getServer() {
        return world instanceof ServerWorld ? ((ServerWorld) world).getServer() : null;
    }

    public AttributeMap getAttributes() {
        return this.attributes;
    }

    public RNG getRng() {
        return this.random;
    }

    /**
     * Gets the pipeline data for this entity.
     * This will be sent to the client when the entity calls {@link #sendPipeline()}.
     *
     * @return the pipeline
     */
    public MapType getPipeline() {
        MapType copy = this.pipeline;
        this.pipeline = new MapType();
        return copy;
    }

    /**
     * Sends the pipeline data for this entity to the client.
     */
    public void sendPipeline() {
        if (this.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            // Send the entity to all tracking players
            MapType pipeline1 = this.getPipeline();
            if (pipeline1.entries().isEmpty()) return;
            serverWorld.sendAllTracking((int) this.x, (int) this.y, (int) this.z, new S2CEntityPipeline(this.getId(), pipeline1));
        }
    }

    /**
     * Called when a pipeline packet is received for this entity.
     *
     * @param pipeline the pipeline data
     */
    public void onPipeline(MapType pipeline) {
        this.x = pipeline.getDouble("x", x);
        this.y = pipeline.getDouble("y", y);
        this.z = pipeline.getDouble("z", z);
    }

    /**
     * Teleports the player to the target entity
     *
     * @param target the target entity
     */
    public void teleportTo(Entity target) {
        this.teleportTo(target.getX(), target.getY(), target.getZ());
    }

    /**
     * Teleports the player to the target position
     *
     * @param target the target position
     */
    public void teleportTo(Vec3d target) {
        this.teleportTo(target.x, target.y, target.z);
    }

    public void teleportDimension(Vec3d position, ServerWorld world) {
        this.getWorld().despawn(this);
        this.teleportTo(position);
        this.onTeleportedDimension(world);
        this.world = world;

        world.spawn(this);
    }

    public void onTeleportedDimension(WorldAccess world) {
        this.world = world;
    }

    public double distanceTo(Entity entity) {
        return distanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public double distanceTo(double x, double y, double z) {
        double a = x - this.x;
        double b = y - this.y;
        double c = z - this.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void onRemoved() {

    }

    public enum Pose {
        IDLE,
        WALKING
    }
}
