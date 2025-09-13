package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.util.RenderObject;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.util.DVec3;
import dev.ultreon.quantum.util.GameObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class RenderEntity extends RenderObject {
    private final Entity entity;
    public @Nullable Gizmo boundsGizmo;
    private final DVec3 tmp = new DVec3();
    private final Vector3 tmp1 = new Vector3();
    private BoundingBox bounds = new BoundingBox();

    public RenderEntity(Entity entity) {
        this.entity = entity;
        this.name = entity.toString();
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RenderEntity) obj;
        return Objects.equals(this.entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity);
    }

    @Override
    public String toString() {
        return "RenderEntity[" +
               "entity=" + entity + ']';
    }

    @Override
    public List<GameObject> hit(Ray ray) {
        QuantumClient quantumClient = QuantumClient.get();
        Vector3 relative = quantumClient.camera.relative(entity.getPosition(tmp), tmp1);
        BoundingBox gdx = entity.getBoundingBox().asGdx(bounds);
        gdx.min.add(relative.x, relative.y, relative.z);
        gdx.max.add(relative.x, relative.y, relative.z);
        if (Intersector.intersectRayBounds(ray, gdx, null)) {
            return entity.hit(ray);
        }
        return super.hit(ray);
    }
}
