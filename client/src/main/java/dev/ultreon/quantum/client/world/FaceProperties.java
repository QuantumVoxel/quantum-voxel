package dev.ultreon.quantum.client.world;

import java.util.Objects;

/**
 * The FaceProperties class represents the properties of a face, such as random rotation.
 * This class includes methods for equality comparison and hashing.
 * It also includes a Builder class to aid in creating instances of FaceProperties with the desired properties.
 */
public class FaceProperties {
    public boolean randomRotation = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        FaceProperties that = (FaceProperties) o;
        return this.randomRotation == that.randomRotation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.randomRotation);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * The Builder class is a helper class to aid in the creation of instances
     * of the FaceProperties class with specified properties.
     * <p>
     * It provides a fluent API for setting different properties and eventually
     * building the FaceProperties instance.
     */
    public static class Builder {
        private final FaceProperties faceProperties = new FaceProperties();

        public Builder randomRotation() {
            this.faceProperties.randomRotation = true;
            return this;
        }

        public FaceProperties build() {
            return this.faceProperties;
        }
    }
}
