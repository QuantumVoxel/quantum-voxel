package dev.ultreon.quantum.client.model.model;

import dev.ultreon.quantum.world.Direction;

import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public final class FaceElement {
    final String texture;
    final UVs uvs;
    private final int rotation;
    private final int tintindex;
    final Direction cullface;

    public FaceElement(String texture, UVs uvs, int rotation, int tintindex,
                       String cullface) {
        this.texture = texture;
        this.uvs = uvs;
        this.rotation = rotation;
        this.tintindex = tintindex;

        if (cullface != null)
            switch (cullface) {
                case "north":
                    this.cullface = Direction.NORTH;
                    break;
                case "south":
                    this.cullface = Direction.SOUTH;
                    break;
                case "east":
                    this.cullface = Direction.EAST;
                    break;
                case "west":
                    this.cullface = Direction.WEST;
                    break;
                case "up":
                    this.cullface = Direction.UP;
                    break;
                case "down":
                    this.cullface = Direction.DOWN;
                    break;
                default:
                    throw new UnsupportedOperationException("Invalid cullface");
            }
        else this.cullface = null;
    }

    public String texture() {
        return texture;
    }

    public UVs uvs() {
        return uvs;
    }

    public int rotation() {
        return rotation;
    }

    public int tintindex() {
        return tintindex;
    }

    public Direction cullface() {
        return cullface;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FaceElement) obj;
        return Objects.equals(this.texture, that.texture) &&
               Objects.equals(this.uvs, that.uvs) &&
               this.rotation == that.rotation &&
               this.tintindex == that.tintindex &&
               Objects.equals(this.cullface, that.cullface);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texture, uvs, rotation, tintindex, cullface);
    }

    @Override
    public String toString() {
        return "FaceElement[" +
               "texture=" + texture + ", " +
               "uvs=" + uvs + ", " +
               "rotation=" + rotation + ", " +
               "tintindex=" + tintindex + ", " +
               "cullface=" + cullface + ']';
    }

}
