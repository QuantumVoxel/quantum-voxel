package dev.ultreon.quantum.entity;

import com.google.common.base.Preconditions;
import dev.ultreon.ubo.types.IntType;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.world.World;

public class DroppedItem extends Entity {
    private ItemStack stack;
    private int age;
    private int pickupDelay = 0;

    public DroppedItem(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);

        this.stack = new ItemStack();
    }

    public DroppedItem(World world, ItemStack stack, Vec3d position, Vec3d velocity) {
        super(EntityTypes.DROPPED_ITEM, world);
        this.stack = stack;
        this.setPosition(position);
        this.setVelocity(velocity);
    }

    @Override
    public void onPrepareSpawn(MapType spawnData) {
        super.onPrepareSpawn(spawnData);

        if (this.stack.isEmpty()) {
            markRemoved();
        } else {
            this.pickupDelay = 40;
            if (spawnData.<IntType>contains("pickupDelay"))
                this.pickupDelay = spawnData.getInt("pickupDelay");

            pipeline.put("Item", this.stack.save());
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;

        pipeline.putInt("age", this.age);
        pipeline.put("Item", this.stack.save());

        if (this.pickupDelay <= 0) {
            for (Entity entity : this.world.entitiesWithinDst(this, 4)) {
                if (entity instanceof Player player) {
                    double deltaX = (player.getX() - this.getX()) / 10;
                    double deltaY = (player.getY() - this.getY()) / 10;
                    double deltaZ = (player.getZ() - this.getZ()) / 10;
                    this.move(deltaX, deltaY, deltaZ);
                }
            }

            for (Entity entity : this.world.collideEntities(this, getBoundingBox().ext(0.5, 0.5, 0.5))) {
                if (this.isMarkedForRemoval()) continue;
                if (entity instanceof ServerPlayer player) {
                    this.markRemoved();
                    player.inventory.addItem(this.stack);
                }
            }
        }

        if (pickupDelay-- <= 0) {
            pickupDelay = 0;
        }

        if (age > 12000) {
            markRemoved();
        }

        sendPipeline();
    }

    @Override
    public void onPipeline(MapType pipeline) {
        super.onPipeline(pipeline);

        this.age = pipeline.getInt("age", this.age);
        MapType item = pipeline.getMap("Item");
        if (item != null) this.stack = ItemStack.load(item);
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        Preconditions.checkNotNull(stack, "Stack cannot be null");
        this.stack = stack;
        this.pipeline.put("Item", stack.save());
    }

    public int getAge() {
        return this.age;
    }

    public int getPickupDelay() {
        return pickupDelay;
    }

    public void setPickupDelay(int pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    @Override
    public MapType save(MapType data) {
        super.save(data);
        data.put("Item", this.stack.save());
        data.putInt("age", this.age);
        return data;
    }

    @Override
    public void load(MapType data) {
        super.load(data);
        this.stack = ItemStack.load(data.getMap("Item"));
        this.age = data.getInt("age");
    }
}
