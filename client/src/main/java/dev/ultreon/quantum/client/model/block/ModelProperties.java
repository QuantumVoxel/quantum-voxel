package dev.ultreon.quantum.client.model.block;

import dev.ultreon.quantum.client.world.FaceProperties;
import dev.ultreon.quantum.world.Direction;

import java.util.Objects;

public class ModelProperties {
    public FaceProperties top;
    public FaceProperties bottom;
    public FaceProperties left;
    public FaceProperties right;
    public FaceProperties front;
    public FaceProperties back;
    public Direction rotation;
    public String renderPass = "opaque";

    public ModelProperties() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ModelProperties that = (ModelProperties) o;
        return Objects.equals(this.top, that.top) && Objects.equals(this.bottom, that.bottom) && Objects.equals(this.left, that.left) && Objects.equals(this.right, that.right) && Objects.equals(this.front, that.front) && Objects.equals(this.back, that.back) && Objects.equals(this.rotation, that.rotation);
    }

    @Override
    public int hashCode() {
        int result = this.top.hashCode();
        result = 31 * result + this.bottom.hashCode();
        result = 31 * result + this.left.hashCode();
        result = 31 * result + this.right.hashCode();
        result = 31 * result + this.front.hashCode();
        result = 31 * result + this.back.hashCode();
        result = 31 * result + this.rotation.hashCode();
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FaceProperties top = new FaceProperties();
        private FaceProperties bottom = new FaceProperties();
        private FaceProperties left = new FaceProperties();
        private FaceProperties right = new FaceProperties();
        private FaceProperties front = new FaceProperties();
        private FaceProperties back = new FaceProperties();
        private Direction horizontalRotation = Direction.NORTH;
        private String renderPass = "opaque";

        public Builder top(FaceProperties top) {
            this.top = top;
            return this;
        }

        public Builder bottom(FaceProperties bottom) {
            this.bottom = bottom;
            return this;
        }

        public Builder left(FaceProperties left) {
            this.left = left;
            return this;
        }

        public Builder right(FaceProperties right) {
            this.right = right;
            return this;
        }

        public Builder front(FaceProperties front) {
            this.front = front;
            return this;
        }

        public Builder back(FaceProperties back) {
            this.back = back;
            return this;
        }

        public Builder renderPass(String renderPass) {
            this.renderPass = renderPass;
            return this;
        }

        public ModelProperties build() {
            ModelProperties modelProperties = new ModelProperties();
            modelProperties.top = this.top;
            modelProperties.bottom = this.bottom;
            modelProperties.left = this.left;
            modelProperties.right = this.right;
            modelProperties.front = this.front;
            modelProperties.back = this.back;
            modelProperties.rotation = this.horizontalRotation;
            modelProperties.renderPass = this.renderPass;
            return modelProperties;
        }

        public Builder rotateHorizontal(Direction direction) {
            this.horizontalRotation = direction;
            return this;
        }
    }
}
