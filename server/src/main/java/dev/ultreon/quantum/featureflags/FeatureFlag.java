package dev.ultreon.quantum.featureflags;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public class FeatureFlag {
    public NamespaceID getId() {
        return Registries.FEATURE_FLAG.getId(this);
    }
}
