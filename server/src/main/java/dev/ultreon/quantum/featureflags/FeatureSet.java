package dev.ultreon.quantum.featureflags;

import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.ubo.types.ListType;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.ubo.types.StringType;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.HashSet;
import java.util.Set;

public class FeatureSet {
    private final Set<FeatureFlag> flags;

    public FeatureSet(Set<FeatureFlag> flags) {
        this.flags = flags;
    }

    public boolean isEnabled(FeatureFlag flag) {
        return this.flags.contains(flag);
    }

    public void save(MapType data) {
        ListType<StringType> enabledFlags = new ListType<>();

        for (FeatureFlag flag : this.flags) {
            enabledFlags.add(new StringType(flag.getId().toString()));
        }

        data.put("EnabledFlags", enabledFlags);
    }

    public static FeatureSet load(MapType data) {
        ListType<StringType> flagsData = data.getList("EnabledFlags", new ListType<>());
        Set<FeatureFlag> enabledFlags = new HashSet<>();
        for (StringType flag : flagsData) {
            enabledFlags.add(Registries.FEATURE_FLAG.get(NamespaceID.parse(flag.getValue())));
        }

        return new FeatureSet(enabledFlags);
    }
}
