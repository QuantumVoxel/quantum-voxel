package dev.ultreon.quantum.featureflags;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public class FeatureFlags {
    public static final FeatureFlag ADVANCED_GRAPHICS = Registries.FEATURE_FLAG.register(NamespaceID.of("advanced_graphics"), new FeatureFlag());
}
