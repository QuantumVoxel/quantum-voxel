package dev.ultreon.quantum.featureflags;

public interface Feature {
    boolean isEnabled(FeatureSet featureSet);
}
