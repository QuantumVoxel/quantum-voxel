package dev.ultreon.quantum.entity;

import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.perms.Permission;
import dev.ultreon.quantum.cs.ComponentSystem;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.entity.util.EntitySize;
import dev.ultreon.quantum.events.EntityEvents;
import dev.ultreon.quantum.events.api.ValueEventResult;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.server.util.Utils;
import dev.ultreon.quantum.text.LanguageBootstrap;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.text.Translations;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.BoundingBoxUtils;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.rng.JavaRNG;
import dev.ultreon.quantum.world.rng.RNG;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.ubo.types.MapType;
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
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @see World#spawn(Entity)
 * @see <a href="https://github.com/Ultreon/quantum-voxel/wiki/Entities">Entities</a>
 */
public class Entity extends ComponentSystem implements CommandSender {
    private final EntityType<? extends Entity> type;
    protected final WorldAccess world;
    protected double x;
    protected double y;
    protected double z;
    public float xRot;
    public float yRot;
    private int id = -1;
    public boolean onGround;
    public double velocityX;
    public double velocityY;
    public double velocityZ;
    public float gravity = 0.09f;
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

    protected void setupAttributes() {

    }

    /**
     * Loads the entire entity data from a MapType object.
     *
     * @param world the world to load the entity in
     * @param data the MapType object to load the data from
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
     *    player connection to bug out.
     *
     * @see #isMarkedForRemoval()
     * @see <a href="https://github.com/Ultreon/quantum-voxel/wiki/Entities#entity-removal">Entity Removal</a>
     */
    public void markRemoved() {
        this.markedForRemoval = true;
    }

    /**
     * Returns whether the entity is marked for removal.
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

    protected void move() {
        this.move(this.velocityX, this.velocityY, this.velocityZ);
    }

    public boolean isAffectedByFluid() {
        return this.isInWater();
    }

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
            return;
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
        // Store the original deltas
        double originalDeltaX = deltaX, originalDeltaY = deltaY, originalDeltaZ = deltaZ;

        // Calculate the absolute values of the deltas
        double absDeltaX = Math.abs(deltaX);
        double absDeltaY = Math.abs(deltaY);
        double absDeltaZ = Math.abs(deltaZ);

        // Check if the deltas are too small to cause a significant move
        if (absDeltaX < 0.001 && absDeltaY < 0.001 && absDeltaZ < 0.001) {
            absDeltaX = 0.0;
            absDeltaY = 0.0;
            absDeltaZ = 0.0;
        }

        // Trigger an event to allow modification of the move
        ValueEventResult<Vec3d> eventResult = EntityEvents.MOVE.factory().onEntityMove(this, deltaX, deltaY, deltaZ);
        Vec3d modifiedValue = eventResult.getValue();

        // If the event is canceled and a modified value is provided, update the deltas
        if (eventResult.isCanceled()) {
            if (modifiedValue != null) {
                deltaX = modifiedValue.x;
                deltaY = modifiedValue.y;
                deltaZ = modifiedValue.z;
            } else {
                return this.isColliding;
            }
        }

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
     * @param ext Bounding box of the entity
     * @param dx Change in x-coordinate
     * @param dy Change in y-coordinate
     * @param dz Change in z-coordinate
     * @param oldDx Original change in x-coordinate
     * @param oldDy Original change in y-coordinate
     * @param oldDz Original change in z-coordinate
     */
    private void moveWithCollision(BoundingBox ext, double dx, double dy, double dz, double oldDx, double oldDy, double oldDz) {
        // Get list of bounding boxes the entity collides with
        List<BoundingBox> boxes = this.world.collide(ext, false);

        BoundingBox pBox = this.getBoundingBox(); // Get the entity's bounding box

        this.isColliding = false;
        this.isCollidingY = false;

        // Check collision and update y-coordinate
        for (BoundingBox box : boxes) {
            double dy2 = BoundingBoxUtils.clipYCollide(box, pBox, dy);
            this.isColliding |= dy != dy2;
            this.isCollidingY |= dy != dy2;
            dy = dy2;
        }

        // Update the y-coordinate of the bounding box
        pBox.min.add(0.0f, dy, 0.0f);
        pBox.max.add(0.0f, dy, 0.0f);
        pBox.update();

        this.isCollidingX = false;

        // Check collision and update x-coordinate
        for (BoundingBox box : boxes) {
            double dx2 = BoundingBoxUtils.clipXCollide(box, pBox, dx);
            this.isColliding |= dx != dx2;
            this.isCollidingX |= dx != dx2;
            dx = dx2;
        }

        // Update the x-coordinate of the bounding box
        pBox.min.add(dx, 0.0f, 0.0f);
        pBox.max.add(dx, 0.0f, 0.0f);
        pBox.update();

        this.isCollidingZ = false;

        // Check collision and update z-coordinate
        for (BoundingBox box : boxes) {
            double dz2 = BoundingBoxUtils.clipZCollide(box, pBox, dz);
            this.isColliding |= dz != dz2;
            this.isCollidingZ |= dz != dz2;
            dz = dz2;
        }

        // Update the z-coordinate of the bounding box
        pBox.min.add(0.0f, 0.0f, dz);
        pBox.max.add(0.0f, 0.0f, dz);
        pBox.update();

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

        // Handle collision responses and update fall distance
        if (this.onGround && !this.wasOnGround) {
            this.hitGround();
            this.fallDistance = 0.0F;
            this.velocityY = 0.0f;
            dy = 0.0f;
        } else if (dy < 0) {
            this.fallDistance -= dy;
        }

        // Reset velocity if there was a collision in z-coordinate
        if (oldDz != dz) {
            this.velocityZ = 0.0f;
        }

        // Update entity's position
        this.x = (pBox.min.x + pBox.max.x) / 2.0f;
        this.y = pBox.min.y;
        this.z = (pBox.min.z + pBox.max.z) / 2.0f;

        this.oDx = dx;
        this.oDy = dy;
        this.oDz = dz;
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
     */
    public boolean isInVoid() {
        return this.y < World.WORLD_DEPTH - 64;
    }

    public BoundingBox getBoundingBox() {
        return this.getBoundingBox(this.getSize());
    }

    @ApiStatus.OverrideOnly
    public BoundingBox getBoundingBox(EntitySize size) {
        return Entity.getBoundingBox(this.getPosition(), size);
    }

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
    }

    public BlockVec getBlockVec() {
        return new BlockVec(this.x, this.y, this.z, BlockVecSpace.WORLD);
    }

    public BlockVec getBlockVec(BlockVecSpace space) {
        return new BlockVec(this.x, this.y, this.z, space);
    }

    public Vec2f getRotation() {
        return new Vec2f(this.xRot, this.yRot);
    }

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

    public void setRotation(Vec2f position) {
        this.xRot = position.x;
        this.yRot = Mth.clamp(position.y, -90, 90);
    }

    public Vec3d getVelocity() {
        return new Vec3d(this.velocityX, this.velocityY, this.velocityZ);
    }

    public void setVelocity(Vec3d velocity) {
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
    }

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
     * @return
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
        if (this.world instanceof ServerWorld serverWorld) {
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
        world.spawn(this);
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

    public enum Pose {
        IDLE,
        WALKING
    }
}
